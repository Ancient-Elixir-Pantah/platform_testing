#  Copyright (C) 2023 The Android Open Source Project
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

"""Constants for Nearby Connection."""

import dataclasses
import datetime
import enum

NEARBY_RESET_WAIT_TIME = datetime.timedelta(seconds=5)
WIFI_DISCONNECTION_DELAY = datetime.timedelta(seconds=3)

FIRST_DISCOVERY_TIMEOUT = datetime.timedelta(seconds=15)
FIRST_CONNECTION_INIT_TIMEOUT = datetime.timedelta(seconds=30)
FIRST_CONNECTION_RESULT_TIMEOUT = datetime.timedelta(seconds=35)
FILE_1M_PAYLOAD_TRANSFER_TIMEOUT = datetime.timedelta(seconds=110)
SECOND_DISCOVERY_TIMEOUT = datetime.timedelta(seconds=25)
SECOND_CONNECTION_INIT_TIMEOUT = datetime.timedelta(seconds=10)
SECOND_CONNECTION_RESULT_TIMEOUT = datetime.timedelta(seconds=25)
CONNECTION_BANDWIDTH_CHANGED_TIMEOUT = datetime.timedelta(seconds=25)
FILE_1G_PAYLOAD_TRANSFER_TIMEOUT = datetime.timedelta(seconds=210)
WIFI_WLAN_CONNECTING_TIME_OUT = datetime.timedelta(seconds=25)
DISCONNECTION_TIMEOUT = datetime.timedelta(seconds=15)

BT_TRANSFER_THROUGHPUT_BENCHMARK_KBS = 10.0  # 10KBps
WIFI_TRANSFER_THROUGHPUT_BENCHMARK_KBS = 10.0 * 1024  # 10MBps

BT_TRANSFER_THROUGHPUT_KBS_PERCENTILE = 95
WIFI_TRANSFER_THROUGHPUT_KBS_PERCENTILE = 95

UNSET_LATENCY = datetime.timedelta.max
UNSET_THROUGHPUT_KBS = -1.0


@enum.unique
class PayloadType(enum.IntEnum):
  FILE = 2
  STREAM = 3


@dataclasses.dataclass(frozen=False)
class TestParameters:
  """Test parameters to be customized for Nearby Connection."""
  test_report_alias_name: str = 'unspecified'
  fast_fail_on_any_error: bool = False
  wifi_country_code: str = ''
  wifi_ssid: str = ''
  wifi_password: str = ''
  toggle_airplane_mode_target_side: bool = True
  disconnect_wifi_after_test: bool = False
  bt_transfer_throughput_benchmark_kbs: float = (
      BT_TRANSFER_THROUGHPUT_BENCHMARK_KBS
  )
  bt_transfer_throughput_kbs_percentile: int = (
      BT_TRANSFER_THROUGHPUT_KBS_PERCENTILE
  )
  wifi_transfer_throughput_benchmark_kbs: float = (
      WIFI_TRANSFER_THROUGHPUT_BENCHMARK_KBS
  )
  wifi_transfer_throughput_kbs_percentile: int = (
      WIFI_TRANSFER_THROUGHPUT_KBS_PERCENTILE
  )
  payload_type: PayloadType = PayloadType.STREAM


@enum.unique
class NearbyMedium(enum.IntEnum):
  AUTO = 0
  BT_ONLY = 1
  BLE_ONLY = 2
  WIFILAN_ONLY = 3
  WIFIAWARE_ONLY = 4
  UPGRADE_TO_WEBRTC = 5
  UPGRADE_TO_WIFIHOTSPOT = 6
  UPGRADE_TO_WIFIDIRECT = 7
  BLE_L2CAP_ONLY = 8
  # including WIFI_WLAN, WIFI_HOTSPOT, WIFI_DIRECT
  UPGRADE_TO_ALL_WIFI = 9


@enum.unique
class NearbyConnectionMedium(enum.IntEnum):
  """The final connection medium selected, see BandWidthInfo.Medium."""
  UNKNOWN = 0
  # reserved 1, it's Medium.MDNS, not used now
  BLUETOOTH = 2
  WIFI_HOTSPOT = 3
  BLE = 4
  WIFI_LAN = 5
  WIFI_AWARE = 6
  NFC = 7
  WIFI_DIRECT = 8
  WEB_RTC = 9
  # 10 is reserved.
  USB = 11


def is_high_quality_medium(medium: NearbyMedium) -> bool:
  return medium in {
      NearbyMedium.WIFILAN_ONLY,
      NearbyMedium.WIFIAWARE_ONLY,
      NearbyMedium.UPGRADE_TO_WEBRTC,
      NearbyMedium.UPGRADE_TO_WIFIHOTSPOT,
      NearbyMedium.UPGRADE_TO_WIFIDIRECT,
      NearbyMedium.UPGRADE_TO_ALL_WIFI,
  }


@enum.unique
class MediumUpgradeType(enum.IntEnum):
  DEFAULT = 0
  DISRUPTIVE = 1
  NON_DISRUPTIVE = 2


@dataclasses.dataclass(frozen=True)
class ConnectionSetupTimeouts:
  """The timeouts of the nearby connection setup."""
  discovery_timeout: datetime.timedelta | None = None
  connection_init_timeout: datetime.timedelta | None = None
  connection_result_timeout: datetime.timedelta | None = None


@dataclasses.dataclass(frozen=False)
class ConnectionSetupQualityInfo:
  """The quality information of the nearby connection setup."""
  discovery_latency: datetime.timedelta = UNSET_LATENCY
  connection_latency: datetime.timedelta = UNSET_LATENCY
  medium_upgrade_latency: datetime.timedelta = UNSET_LATENCY
  medium_upgrade_expected: bool = False
  upgrade_medium: NearbyConnectionMedium | None = None

  def __str__(self):
    desc = (
        f'discovery: {round(self.discovery_latency.total_seconds(), 1)}s'
        + f', connection: {round(self.connection_latency.total_seconds(), 1)}s'
    )
    if self.medium_upgrade_latency is not UNSET_LATENCY:
      desc += (
          ', upgrade:'
          + f'{round(self.medium_upgrade_latency.total_seconds(), 1)}s'
      )
    if self.upgrade_medium:
      desc += f', medium: {self.upgrade_medium.name}'
    return desc


@dataclasses.dataclass(frozen=False)
class SingleTestResult:
  """The test result of a single iteration."""

  first_connection_setup_quality_info: ConnectionSetupQualityInfo = (
      dataclasses.field(default_factory=ConnectionSetupQualityInfo)
  )
  first_bt_transfer_throughput_kbs: float = UNSET_THROUGHPUT_KBS
  discoverer_wifi_wlan_latency: datetime.timedelta = UNSET_LATENCY
  second_connection_setup_quality_info: ConnectionSetupQualityInfo = (
      dataclasses.field(default_factory=ConnectionSetupQualityInfo)
  )
  second_wifi_transfer_throughput_kbs: float = UNSET_THROUGHPUT_KBS
  advertiser_wifi_wlan_latency: datetime.timedelta = UNSET_LATENCY
  discoverer_wifi_wlan_expected: bool = False
  advertiser_wifi_wlan_expected: bool = False


@dataclasses.dataclass(frozen=True)
class ResultStats:
  reach_target: bool = False
  reach_rate: float = 0.0
  success_rate: float = 0.0


@dataclasses.dataclass(frozen=True)
class FailTargetSummary:
  name: str = ''
  rate: float = 0.0
  goal: float = 0.0


@dataclasses.dataclass(frozen=False)
class QuickStartTestMetrics:
  """Metrics data for quick start test."""
  first_discovery_latencies: list[datetime.timedelta] = dataclasses.field(
      default_factory=list[datetime.timedelta])
  first_connection_latencies: list[datetime.timedelta] = dataclasses.field(
      default_factory=list[datetime.timedelta])
  discoverer_wifi_wlan_latencies: list[
      datetime.timedelta] = dataclasses.field(
          default_factory=list[datetime.timedelta])
  bt_transfer_throughputs_kbs: list[float] = dataclasses.field(
      default_factory=list[float])
  second_discovery_latencies: list[datetime.timedelta] = dataclasses.field(
      default_factory=list[datetime.timedelta]
  )
  second_connection_latencies: list[datetime.timedelta] = dataclasses.field(
      default_factory=list[datetime.timedelta])
  second_medium_upgrade_latencies: list[
      datetime.timedelta] = dataclasses.field(
          default_factory=list[datetime.timedelta])
  advertiser_wifi_wlan_latencies: list[
      datetime.timedelta] = dataclasses.field(
          default_factory=list[datetime.timedelta])
  wifi_transfer_throughputs_kbs: list[float] = dataclasses.field(
      default_factory=list[float])