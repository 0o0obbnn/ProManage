# Technical Debt Task: Fix Failing Test Suite in `promanage-service` Module

**Priority**: High
**Status**: To Do
**Reporter**: Gemini Bug-Fixing Expert
**Date**: 2025-10-22

---

### Description

During the process of fixing compilation errors, a full `mvn clean install` revealed that the test suite within the `promanage-service` module is fundamentally broken. A large number of tests are failing with a wide variety of errors, preventing the build from completing successfully without skipping tests.

This technical debt poses a significant risk to the project's stability and makes it difficult to verify future changes.

### Affected Module

- `backend/promanage-service`

### Summary of Test Failures

The build log shows over 20 failing tests, with errors including but not limited to:

- **`java.lang.IllegalStateException: Unable to find a @SpringBootConfiguration`**: Integration tests are missing necessary Spring Boot context configuration.
- **`com.promanage.common.exception.BusinessException: 用户未登录`**: Unit tests are not mocking the security context, causing security checks in service methods to fail.
- **`java.lang.NullPointerException`**: Multiple tests fail due to NPEs, indicating missing mocks for injected dependencies (e.g., in `OrganizationServiceImplTest`, `ProjectActivityServiceImplTest`).
- **Mockito `Wanted but not invoked` errors**: Mocks are configured, but the code path that uses them is never reached, often due to earlier exceptions (like the security check failure).
- **`org.opentest4j.AssertionFailedError`**: Basic test assertions are failing, indicating regressions or incorrect test logic.
- **Mockito `UnnecessaryStubbingException`**: Tests have declared mock behaviors that are never used, indicating sloppy test code.

### Acceptance Criteria

- All unit and integration tests in the `promanage-service` module pass successfully.
- `mvn clean install` runs successfully without skipping any tests (`-DskipTests=false`, `-Dpmd.skip=false`, `-Dspotbugs.skip=false`).
- The `@Disabled` annotations added to `UserServiceImplTest` have been removed, and the tests now pass.

### Recommended Actions

1.  **Add `mockito-inline` Dependency**: Add the `org.mockito:mockito-inline` dependency to the test scope in the relevant `pom.xml` file(s) to properly support mocking of static methods (`SecurityUtils.getCurrentUserId`).
2.  **Fix Security Context Mocks**: Systematically add security context mocking to all tests for services that require an authenticated user. This was the root cause of many failures.
3.  **Fix Integration Tests**: Add the required `@SpringBootTest` or `@ContextConfiguration` annotations to all integration tests that are currently failing to load the Spring context.
4.  **Incremental Repair**: Tackle the remaining test failures class by class. Prioritize fixing tests with `NullPointerException` and `AssertionFailedError` as they may indicate real bugs.
5.  **Clean Up**: Remove unnecessary stubs and fix the underlying issues causing Mockito verification errors.

---
