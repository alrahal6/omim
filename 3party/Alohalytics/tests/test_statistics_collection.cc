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

#include "gtest/gtest.h"

#include "../src/alohalytics.h"

#include <condition_variable>
#include <cstdint>
#include <mutex>

using namespace std;

TEST(StatisticsCollection, SmokeTest) {
  alohalytics::Stats & stats = alohalytics::Stats::Instance();
  stats.SetChannelsCount(3);

  alohalytics::Location location;
  location.SetLatLon(123456789L, -14.1234567, 133.1234567, 3.52);
  stats.LogEvent("SimulatedLocationEvent", {{"somekey", "somevalue"}}, location, alohalytics::ChannelMask(0));
  stats.LogEvent("TestEvent", alohalytics::ChannelMask(1));

  vector<bool> expectedResults = {true, true, false};
  for (uint32_t channel = 0; channel < expectedResults.size(); ++channel) {
    mutex m;
    condition_variable cv;
    bool finished = false;
    bool hasCollectedData = false;
    stats.CollectBlobsToUpload(true /* delete_files */, [&](std::vector<std::string> & blobs) {
      unique_lock<mutex> lock(m);
      finished = true;
      hasCollectedData = !blobs.empty();
      cv.notify_one();
    }, alohalytics::ChannelMask(channel));

    unique_lock<mutex> lock(m);
    cv.wait(lock, [&]{return finished;});
    EXPECT_EQ(hasCollectedData, expectedResults[channel]);
  }
}
