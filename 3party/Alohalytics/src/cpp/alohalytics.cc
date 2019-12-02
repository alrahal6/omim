/*******************************************************************************
 The MIT License (MIT)

 Copyright (c) 2015 Alexander Zolotarev <me@alex.bio> from Minsk, Belarus

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 *******************************************************************************/

// Used to avoid cereal compilation issues on iOS/MacOS when check() macro is defined.
#ifdef __APPLE__
#define __ASSERT_MACROS_DEFINE_VERSIONS_WITHOUT_UNDERSCORES 0
#endif

#include <cassert>
#include <cerrno>
#include <cstdio>  // remove

#include "../alohalytics.h"
#include "../file_manager.h"
#include "../gzip_wrapper.h"
#include "../http_client.h"
#include "../logger.h"

// TODO(AlexZ): Refactor out cereal library - it's too heavy overkill for us.
#include "../cereal/include/archives/binary.hpp"
#include "../cereal/include/types/string.hpp"
#include "../cereal/include/types/map.hpp"

#define LOG_IF_DEBUG(...)                                  \
  if (debug_mode_) {                                       \
    if (enabled_) {                                        \
      alohalytics::Logger().Log(__VA_ARGS__);              \
    } else {                                               \
      alohalytics::Logger().Log("Disabled:", __VA_ARGS__); \
    }                                                      \
  }

namespace alohalytics {

static constexpr const char * kAlohalyticsHTTPContentType = "application/alohalytics-binary-blob";

uint32_t ChannelsCount(uint32_t channels_mask) {
  uint32_t channels_count = 0;
  auto m = channels_mask;
  while (m != 0)
  {
    if (m & 1)
      channels_count++;
    m = m >> 1;
  }
  return channels_count;
}

// Use alohalytics::Stats::Instance() to access statistics engine.
Stats::Stats() {
  SetChannelsCount(1);
}

void Stats::Disable() {
  LOG_IF_DEBUG("Statistics collection disabled.");
  enabled_ = false;
}
// Turn back on events collection and sending after Disable();
void Stats::Enable() {
  LOG_IF_DEBUG("Statistics collection enabled.");
  enabled_ = true;
}

void Stats::GzipAndArchiveFileInTheQueue(const std::string & in_file, const std::string & out_archive) const {
  std::string encoded_unique_client_id;
  if (unique_client_id_.empty()) {
    LOG_IF_DEBUG(
        "Warning: unique client id was not set in GzipAndArchiveFileInTheQueue,"
        "statistics will be completely anonymous and hard to process on the server.");
  } else {
    // Pre-calculation for special ID event.
    // We do it for every archived file to have a fresh timestamp.
    encoded_unique_client_id = SerializeUniqueClientId();
  }
  LOG_IF_DEBUG("Archiving", in_file, "to", out_archive);
  // Append unique installation id in the beginning of each archived file.

  try {
    std::string buffer(std::move(encoded_unique_client_id));
    {
      std::ifstream fi;
      fi.exceptions(std::ifstream::failbit | std::ifstream::badbit);
      fi.open(in_file, std::ifstream::in | std::ifstream::binary);
      const size_t data_offset = buffer.size();
      const uint64_t file_size = FileManager::GetFileSize(in_file);
      if (file_size > std::numeric_limits<std::string::size_type>::max()
          || file_size > std::numeric_limits<std::streamsize>::max()) {
        throw std::out_of_range("File size is out of range.");
      }
      buffer.resize(data_offset + size_t(file_size));
      fi.read(&buffer[data_offset], static_cast<std::streamsize>(file_size));
    }
    {
      std::ofstream fo;
      fo.exceptions(std::ifstream::failbit | std::ifstream::badbit);
      fo.open(out_archive, std::ofstream::out | std::ofstream::binary | std::ofstream::trunc);
      const std::string gzipped_buffer = Gzip(buffer);
      std::string().swap(buffer);  // Free memory.
      fo.write(gzipped_buffer.data(), gzipped_buffer.size());
    }
  } catch (const std::exception & ex) {
    LOG_IF_DEBUG("CRITICAL ERROR: Exception in GzipAndArchiveFileInTheQueue:", ex.what());
    LOG_IF_DEBUG("All data collected in", in_file, "will be lost.");
  }
  const int result = std::remove(in_file.c_str());
  if (0 != result) {
    LOG_IF_DEBUG("CRITICAL ERROR: std::remove", in_file, "has failed with error", result, "and errno", errno);
  }
}
  
std::string Stats::GzipInMemoryBuffer(const std::string & buffer) const {
  if (unique_client_id_.empty()) {
    LOG_IF_DEBUG("Warning: unique client id was not set in GzipInMemoryBuffer,"
                 "statistics will be completely anonymous and hard to process on the server.");
  }
  return alohalytics::Gzip(SerializeUniqueClientId() + buffer);
}
  
std::string Stats::SerializeUniqueClientId() const {
  AlohalyticsIdEvent event;
  event.id = unique_client_id_;
  std::ostringstream ostream;
  { cereal::BinaryOutputArchive(ostream) << std::unique_ptr<AlohalyticsBaseEvent, NoOpDeleter>(&event); }
  return ostream.str();
}

Stats & Stats::Instance() {
  static Stats alohalytics;
  return alohalytics;
}

// Easier integration if enabled.
Stats & Stats::SetDebugMode(bool enable) {
  debug_mode_ = enable;
  LOG_IF_DEBUG("Enabled debug mode.");
  return *this;
}

Stats & Stats::SetChannelsCount(size_t channels_count) {
  assert(channels_count >= channels_.size());
  for (size_t i = channels_.size(); i < channels_count; ++i)
    channels_.emplace_back(new Channel(std::bind(&Stats::GzipAndArchiveFileInTheQueue, this, std::placeholders::_1, std::placeholders::_2)));
  return *this;
}

Stats & Stats::SetServerUrls(const std::vector<std::string> & urls_to_upload_statistics_to) {
  assert(channels_.size() == urls_to_upload_statistics_to.size());
  for (size_t i = 0; i < channels_.size(); ++i) {
    channels_[i]->upload_url_ = urls_to_upload_statistics_to[i];
    LOG_IF_DEBUG("Set upload url:", channels_[i]->upload_url_, "for channel", i);
  }
  return *this;
}

Stats & Stats::SetStoragePaths(const std::vector<std::string> & full_paths_to_storage_with_a_slash_at_the_end) {
  assert(channels_.size() == full_paths_to_storage_with_a_slash_at_the_end.size());
  for (size_t i = 0; i < channels_.size(); ++i) {
    channels_[i]->messages_queue_.SetStorageDirectory(full_paths_to_storage_with_a_slash_at_the_end[i]);
    LOG_IF_DEBUG("Set storage path:", full_paths_to_storage_with_a_slash_at_the_end[i], "for channel", i);
  }
  return *this;
}

Stats & Stats::SetClientId(const std::string & unique_client_id) {
  LOG_IF_DEBUG("Set unique client id:", unique_client_id);
  unique_client_id_ = unique_client_id;
  return *this;
}

void Stats::LogEventImpl(AlohalyticsBaseEvent const & event, uint32_t channels_mask) {
  std::ostringstream sstream;
  {
    // unique_ptr is used to correctly serialize polymorphic types.
    cereal::BinaryOutputArchive(sstream) << std::unique_ptr<AlohalyticsBaseEvent const, NoOpDeleter>(&event);
  }
  for (uint32_t i = 0; i < channels_.size(); ++i) {
    if (channels_mask & ChannelMask(i))
      channels_[i]->messages_queue_.PushMessage(sstream.str());
  }
}

void Stats::LogEvent(std::string const & event_name, uint32_t channels_mask) {
  LOG_IF_DEBUG("LogEvent:", event_name);
  if (enabled_) {
    AlohalyticsKeyEvent event;
    event.key = event_name;
    LogEventImpl(event, channels_mask);
  }
}

void Stats::LogEvent(std::string const & event_name, Location const & location, uint32_t channels_mask) {
  LOG_IF_DEBUG("LogEvent:", event_name, location.ToDebugString());
  if (enabled_) {
    AlohalyticsKeyLocationEvent event;
    event.key = event_name;
    event.location = location;
    LogEventImpl(event, channels_mask);
  }
}

void Stats::LogEvent(std::string const & event_name, std::string const & event_value, uint32_t channels_mask) {
  LOG_IF_DEBUG("LogEvent:", event_name, "=", event_value);
  if (enabled_) {
    AlohalyticsKeyValueEvent event;
    event.key = event_name;
    event.value = event_value;
    LogEventImpl(event, channels_mask);
  }
}

void Stats::LogEvent(std::string const & event_name, std::string const & event_value, Location const & location,
                     uint32_t channels_mask) {
  LOG_IF_DEBUG("LogEvent:", event_name, "=", event_value, location.ToDebugString());
  if (enabled_) {
    AlohalyticsKeyValueLocationEvent event;
    event.key = event_name;
    event.value = event_value;
    event.location = location;
    LogEventImpl(event, channels_mask);
  }
}

void Stats::LogEvent(std::string const & event_name, TStringMap const & value_pairs, uint32_t channels_mask) {
  LOG_IF_DEBUG("LogEvent:", event_name, "=", value_pairs);
  if (enabled_) {
    AlohalyticsKeyPairsEvent event;
    event.key = event_name;
    event.pairs = value_pairs;
    LogEventImpl(event, channels_mask);
  }
}

void Stats::LogEvent(std::string const & event_name, TStringMap const & value_pairs, Location const & location,
                     uint32_t channels_mask) {
  LOG_IF_DEBUG("LogEvent:", event_name, "=", value_pairs, location.ToDebugString());
  if (enabled_) {
    AlohalyticsKeyPairsLocationEvent event;
    event.key = event_name;
    event.pairs = value_pairs;
    event.location = location;
    LogEventImpl(event, channels_mask);
  }
}

void Stats::Upload(const TFileProcessingFinishedCallback & upload_finished_callback) {
  if (enabled_) {
    uint32_t channels_count = 0;
    for (const auto & c : channels_) {
      if (c->upload_url_.empty()) {
        LOG_IF_DEBUG("Warning: upload server url has not been set, nothing was uploaded.");
        continue;
      }
      channels_count++;
    }

    auto upload_counter = std::make_shared<uint32_t>(0);
    auto upload_result = std::make_shared<ProcessingResult>(ProcessingResult::ENothingToProcess);
    auto cb = [this, channels_count, upload_counter, upload_result, upload_finished_callback](ProcessingResult result) {
      bool need_notify = false;
      ProcessingResult r = ProcessingResult::ENothingToProcess;
      {
        std::lock_guard<std::mutex> lock(upload_mutex_);
        (*upload_counter)++;
        // Error in any upload - error for the whole process.
        if (result == ProcessingResult::EProcessingError)
          *upload_result = result;
        if (*upload_result == ProcessingResult::ENothingToProcess)
          *upload_result = result;

        if (*upload_counter == channels_count)
        {
          need_notify = true;
          r = *upload_result;
        }
      }
      if (upload_finished_callback && need_notify)
        upload_finished_callback(r);
    };
    for (auto & c : channels_) {
      if (c->upload_url_.empty())
        continue;
      LOG_IF_DEBUG("Trying to upload collected statistics to", c->upload_url_);
      c->messages_queue_.ProcessArchivedFiles(
        std::bind(&Stats::UploadFileImpl, this, c->upload_url_, std::placeholders::_1, std::placeholders::_2),
        true /* delete_after_processing */, cb);
    }
  } else {
    LOG_IF_DEBUG("Statistics is disabled. Nothing was uploaded.");
  }
}

bool Stats::UploadFileImpl(const std::string & upload_url, bool file_name_in_content, const std::string & content) {
  // This code should never be called if upload_url_ was not set.
  assert(!upload_url.empty());
  HTTPClientPlatformWrapper request(upload_url);
  request.set_debug_mode(debug_mode_);

  try {
    if (file_name_in_content) {
      request.set_body_file(content, kAlohalyticsHTTPContentType, "POST", "gzip");
    } else {
      request.set_body_data(GzipInMemoryBuffer(content), kAlohalyticsHTTPContentType, "POST", "gzip");
    }
    const bool uploadSucceeded = request.RunHTTPRequest() && 200 == request.error_code() && !request.was_redirected();
    LOG_IF_DEBUG("RunHTTPRequest has returned code", request.error_code(),
                 request.was_redirected() ? "and request was redirected to " + request.url_received() : " ");
    return uploadSucceeded;
  } catch (const std::exception & ex) {
    LOG_IF_DEBUG("Exception in UploadFileImpl:", ex.what());
  }
  return false;
}

void Stats::CollectBlobsToUpload(bool delete_files, TGetBlobResultCallback result_callback, uint32_t channels_mask) {
  auto result = std::make_shared<std::vector<std::string>>();
  if (channels_mask == 0) {
    if (result_callback)
      result_callback(*result);
    return;
  }
  if (enabled_) {
    LOG_IF_DEBUG("Trying to get blobs for collected statistics");
    auto file_processor = [this, result](bool content_is_file_name, const std::string & content) {
      try {
        if (content_is_file_name) {
          std::string buffer;
          try {
            std::ifstream fi;
            fi.exceptions(std::ifstream::failbit | std::ifstream::badbit);
            fi.open(content, std::ifstream::in | std::ifstream::binary);
            const uint64_t file_size = FileManager::GetFileSize(content);
            if (file_size > std::numeric_limits<std::string::size_type>::max()
                || file_size > std::numeric_limits<std::streamsize>::max()) {
              throw std::out_of_range("File size is out of range.");
            }
            buffer.resize(size_t(file_size));
            fi.read(&buffer[0], static_cast<std::streamsize>(file_size));
          } catch (const std::exception & ex) {
            LOG_IF_DEBUG(ex.what());
          }
          std::lock_guard<std::mutex> lock(collect_mutex_);
          result->push_back(std::move(buffer));
        } else {
          std::lock_guard<std::mutex> lock(collect_mutex_);
          result->push_back(GzipInMemoryBuffer(content));
        }
        return true;
      } catch (const std::exception & ex) {
        LOG_IF_DEBUG("Exception in GetBlobImpl:", ex.what());
      }
      return false;
    };

    const auto channels_count = ChannelsCount(channels_mask);
    auto collect_counter = std::make_shared<uint32_t>(0);
    auto finish_callback =
      [this, result, result_callback = std::move(result_callback),
       collect_counter, channels_count](ProcessingResult) {
      bool need_notify = false;
      {
        std::lock_guard<std::mutex> lock(collect_mutex_);
        (*collect_counter)++;
        if (*collect_counter == channels_count)
          need_notify = true;
      }
      if (result_callback && need_notify)
        result_callback(*result);
    };

    for (uint32_t i = 0; i < channels_.size(); ++i) {
      if (channels_mask & ChannelMask(i))
        channels_[i]->messages_queue_.ProcessArchivedFiles(file_processor, delete_files, finish_callback);
    }
  } else {
    LOG_IF_DEBUG("Statistics is disabled. Nothing was collected.");
  }
}

}  // namespace alohalytics
