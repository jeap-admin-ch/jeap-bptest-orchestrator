# Changelog

## [3.20.0] - 2025-01-15

### Changed

- Switched to a multi-module project and added the module jeap-bptest-orchestrator-instance which will instantiate
  a Business Process Test Orchestrator instance when used as parent project.
- Updated parent from 26.21.1 to 26.22.3 

## [3.19.0] - 2024-12-19

### Changed

- Update parent from 26.4.0 to 26.21.1 (spring boot 3.4.0)

## [3.18.0] - 2024-10-31

### Changed

- Update parent from 26.4.0 to 26.13.0
- Update jeap-bptestagent-api 2.16.0 to 2.17.0

## [3.17.0] - 2024-10-17

### Changed

- Update parent from 26.3.0 to 26.4.0

## [3.16.0] - 2024-09-20

### Changed

- Update parent from 26.0.0 to 26.3.0

## [3.15.0] - 2024-09-06

### Changed

- Update parent from 25.4.0 to 26.0.0

## [3.14.0] - 2024-08-22

### Changed

- Update parent from 24.5.0 to 25.4.0

## [3.13.1] - 2024-07-18

### Changed

- Add flyway PG dependency

## [3.13.0] - 2024-07-16

### Changed

- Update parent from 23.12.0 to 24.5.0

## [3.12.0] - 2024-03-28

### Changed

- Update parent from 23.10.4 to 23.12.0

## [3.11.0] - 2024-03-14

### Changed

- Update parent from 23.6.1 to 23.10.4

## [3.10.0] - 2024-02-27

### Changed

- Updated jeap spring boot parent from 23.0.0 to 23.6.1
- Replaced WebClient with RestClient and WebClientTest with MockMvc

## [3.9.0] - 2024-02-05

### Changed

- Update parent from 22.5.0 to 23.0.0

## [3.8.0] - 2024-01-25

### Changed

- Update parent from 22.4.0 to 22.5.0

## [3.7.0] - 2024-01-25

### Changed

- Update parent from 22.2.3 to 22.4.0

## [3.6.0] - 2024-01-23

### Changed

- Update parent from 22.2.2 to 22.2.3

## [3.5.0] - 2024-01-23

### Changed

- Update parent from 22.1.0 to 22.2.2

## [3.4.0] - 2024-01-16

### Changed

- Update parent from 22.0.0 to 22.1.0

## [3.3.0] - 2024-01-09

### Changed

- Update parent from 21.2.0 to 22.0.0

## [3.2.0] - 2023-12-15

### Changed

- Update parent from 21.0.0 to 21.2.0

## [3.1.2] - 2023-11-28

### Changed

- fix dependency cycle bug

## [3.1.1] - 2023-11-23

### Changed

- enhance abort long running test run to still verify test results and clean up
- enhance abort test run to still verify test results and clean up

## [3.1.0] - 2023-11-22

### Changed

- Update parent from 20.0.2 to 21.0.0

## [3.0.1] - 2023-08-25

### Changed

- Set spring.jpa.properties.hibernate.timezone.default_storage=NORMALIZE

## [3.0.0] - 2023-08-22

### Changed

- Spring Boot 3 Migration

## [2.7.0] - 2023-08-09

### Changed

- Update parent from 19.16.1 to 19.17.0

## [2.6.0] - 2023-08-08

### Changed

- Update parent from 19.14.2 to 19.16.1

## [2.5.1] - 2023-07-06

### Changed

- enhance abort test run to still verify test results until the abort
- Update parent from 19.12.1 to 19.14.2

## [2.5.0] - 2023-05-30

### Changed

- Update parent from 19.10.1 to 19.12.1

## [2.4.0] - 2023-04-21

### Changed

- Update parent from 19.6.0 to 19.10.1
- Update bptestagent-api from 1.8.0 to 1.9.0

## [2.3.0] - 2023-03-21

### Changed

- Update parent from 19.2.0 to 19.6.0
- Field detail in TestResult entity is now nullable

## [2.2.1] - 2023-03-06

### Changed

- introduce configuration of testagents readtimeout
- stop watchdog timer when testcase ended

## [2.2.0] - 2023-02-21

### Changed

- Update parent from 18.4.0 to 19.2.0

## [2.1.0] - 2022-11-28

### Changed

- Update parent from 18.2.0 to 18.4.0

## [2.0.0] - 2022-11-01

### Changed
- Upgrade to Java 17
- New Metrics Page

## [1.9.0] - 2022-10-31

### Changed

- Update parent from 18.0.0 to 18.2.0

## [1.8.0] - 2022-10-04

### Changed

- Update parent from 17.3.0 to 18.0.0 (spring boot 2.7)

## [1.7.0] - 2022-09-21

### Changed

- Update parent from 17.2.2 to 17.3.0

## [1.6.0] - 2022-09-13

### Changed

- Update parent from 15.10.0 to 17.2.2

## [1.5.0] - 12.05.2022

###

* TestRunService: add method to get the overall conclusion of a test run for a given testId
* update to jeap-spring-boot-parent 15.10.0

## [1.4.0] - 28.04.2022

###

* Introduce new application event ReportCreatedEvent, to notify about the result of an executed testcase 


## [1.3.6] - 08.04.2022

###

* TestAgentException now contains test agent name and request url, for more informative logs

## [1.3.5] - 08.04.2022

###

* TestCaseService returns the generated testId 


## [1.3.4] - 19.01.2022

###

* update to jeap-spring-boot-parent 15.4.0

## [1.3.3] - 23.12.2021

### Changed

* update to jeap-spring-boot-parent 15.2.0 (spring boot 2.6.2)

## [1.3.1] - 18.11.2021

### Changed

* If a TestAgent doesn't answer or the answer takes to long (>5 sec), the test will be cancelled (aborted). In JIRA
  Zeypyr the test will be marked as failed.
* Added new Timeout-Property: orchestrator.testRunTimeout (30000 --> 30seconds). If a TestAgent does not notify the
  orchestrator in this time, the Testrun will be marked as failed.

## [1.2.2] - 20.08.2021

### Changed

* Log verify/cleanup errors

## [1.2.1] - 10.08.2021

### Changed

* Fixed Zephyr integration, added logging

## [1.2.0] - 06.08.2021

### Changed

* Add notification & test run services to simplify test cases and fix transaction handling

## [1.1.2] - 05.08.2021

### Changed

* Updated to latest API to improve logging of DTOs

## [1.1.1] - 05.08.2021

### Changed

* Added logging for outgoing test agent calls
* Fixed web clients not created using Spring Builder and this missing caller headers

## [1.1.0] - 22.07.2021

### Added

* Added 'assertActsCalled' in the TestCaseMockTool

## [1.0.0] - 09.07.2021

### Added

* Initial Version
