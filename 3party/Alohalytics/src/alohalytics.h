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

#ifndef ALOHALYTICS_H
#define ALOHALYTICS_H

#include "event_base.h"
#include "location.h"
#include "messages_queue.h"

#include <cstdint>
#include <functional>
#include <list>
#include <map>
#include <memory>
#include <mutex>
#include <string>
#include <vector>

namespace alohalytics {

typedef std::map<std::string, std::string> TStringMap;
typedef std::function<void(std::vector<std::string> & blobs)> TGetBlobResultCallback;
constexpr uint32_t kAllChannels = ~static_cast<uint32_t>(0);
constexpr uint32_t ChannelMask(uint32_t index) {
  return 1 << index;
}
extern uint32_t ChannelsCount(uint32_t channels_mask);

class Stats final {
  // Is statistics engine enabled or disabled.
  // Used if users want to opt-out from events collection.
  bool enabled_ = true;
  // Unique client id is inserted as a special event in the beginning of every archived file before gzipping it.
  // In current implementation it is used to distinguish between different users in the events stream on the server.
  // NOTE: Statistics will not be uploaded if unique client id was not set.
  std::string unique_client_id_;
  struct Channel
  {
    std::string upload_url_;
    THundredKilobytesFileQueue messages_queue_;

    explicit Channel(TFileArchiver const & file_archiver) : messages_queue_(file_archiver) {}
  };
  std::mutex upload_mutex_;
  std::mutex collect_mutex_;
  bool debug_mode_ = false;
  // The order of fields is important. Mutexes must be destroyed after threads (inside channels) in which they are used.
  std::vector<std::unique_ptr<Channel>> channels_;

  // Use alohalytics::Stats::Instance() to access statistics engine.
  Stats();

  // Should return false on upload error.
  bool UploadFileImpl(const std::string & upload_url, bool file_name_in_content, const std::string & content);

  // Called by the queue when file size limit was hit or immediately before file is sent to a server.
  // in_file will be:
  // - Gzipped.
  // - Saved as out_archive for easier post-processing (e.g. uploading).
  // - Deleted.
  void GzipAndArchiveFileInTheQueue(const std::string & in_file, const std::string & out_archive) const;
  std::string GzipInMemoryBuffer(const std::string & buffer) const;
  std::string SerializeUniqueClientId() const;

  void LogEventImpl(AlohalyticsBaseEvent const & event, uint32_t channels_mask);

 public:
  static Stats & Instance();

  // Easier integration if enabled.
  Stats & SetDebugMode(bool enable);
  bool DebugMode() const { return debug_mode_; }

  // Turn off events collection and sending.
  void Disable();
  // Turn back on events collection and sending after Disable();
  void Enable();

  // Set number of channels. Default value is 1. Maximum channels count is 32. Decreasing is forbidden.
  Stats & SetChannelsCount(size_t channels_count);

  // If not set, collected data will never be uploaded.
  Stats & SetServerUrls(const std::vector<std::string> & urls_to_upload_statistics_to);

  // If not set, data will be stored in memory only.
  Stats & SetStoragePaths(const std::vector<std::string> & full_paths_to_storage_with_a_slash_at_the_end);

  // If not set, data will never be uploaded.
  // TODO(AlexZ): Should we allow anonymous statistics uploading?
  Stats & SetClientId(const std::string & unique_client_id);

  void LogEvent(std::string const & event_name, uint32_t channels_mask = ChannelMask(0));
  void LogEvent(std::string const & event_name, Location const & location, uint32_t channels_mask = ChannelMask(0));

  void LogEvent(std::string const & event_name, std::string const & event_value, uint32_t channels_mask = ChannelMask(0));
  void LogEvent(std::string const & event_name, std::string const & event_value, Location const & location,
                uint32_t channels_mask = ChannelMask(0));

  void LogEvent(std::string const & event_name, TStringMap const & value_pairs, uint32_t channels_mask = ChannelMask(0));
  void LogEvent(std::string const & event_name, TStringMap const & value_pairs, Location const & location,
                uint32_t channels_mask = ChannelMask(0));

  // Uploads all previously collected data to the server.
  void Upload(const TFileProcessingFinishedCallback & upload_finished_callback = TFileProcessingFinishedCallback());

  // Calls |result_callback| on the data blobs that are going to be uploaded to the server.
  void CollectBlobsToUpload(bool delete_files, TGetBlobResultCallback result_callback = {},
                            uint32_t channels_mask = ChannelMask(0));
};

inline void LogEvent(std::string const & event_name, uint32_t channels_mask = ChannelMask(0)) {
  Stats::Instance().LogEvent(event_name, channels_mask);
}
inline void LogEvent(std::string const & event_name, Location const & location,
                     uint32_t channels_mask = ChannelMask(0)) {
  Stats::Instance().LogEvent(event_name, location, channels_mask);
}

inline void LogEvent(std::string const & event_name, std::string const & event_value,
                     uint32_t channels_mask = ChannelMask(0)) {
  Stats::Instance().LogEvent(event_name, event_value, channels_mask);
}
inline void LogEvent(std::string const & event_name, std::string const & event_value,
                     Location const & location, uint32_t channels_mask = ChannelMask(0)) {
  Stats::Instance().LogEvent(event_name, event_value, location, channels_mask);
}

inline void LogEvent(std::string const & event_name, TStringMap const & value_pairs,
                     uint32_t channels_mask = ChannelMask(0)) {
  Stats::Instance().LogEvent(event_name, value_pairs, channels_mask);
}
inline void LogEvent(std::string const & event_name, TStringMap const & value_pairs,
                     Location const & location, uint32_t channels_mask = ChannelMask(0)) {
  Stats::Instance().LogEvent(event_name, value_pairs, location, channels_mask);
}

}  // namespace alohalytics

#endif  // #ifndef ALOHALYTICS_H
