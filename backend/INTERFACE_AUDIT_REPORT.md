# Backend Interface Audit Report
**Date**: 2025-10-31
**Status**: 160 compilation errors remaining
**Phase**: Systematic Interface/Implementation Alignment

---

## Executive Summary

After fixing 850+ basic compilation errors (imports, type conversions, entity fields), we've exposed **interface/implementation contract violations** across the service layer. This audit maps all mismatches to enable systematic, batch-based fixes.

### Error Distribution by Severity

| Severity | Count | Description |
|----------|-------|-------------|
| 🔴 **Critical** | 38 | Missing abstract methods, wrong return types |
| 🟡 **High** | 94 | @Override signature mismatches |
| 🟢 **Medium** | 28 | Missing DTO fields, type conversions |

---

## 🔴 Critical Issues (Must Fix First)

### 1. TestCaseServiceImpl.java (38 errors)

**Problem**: Interface contract violations

#### Missing Abstract Method
```java
// INTERFACE (ITestCaseService.java:279)
TestCaseImportResult importTestCases(Long projectId, String fileUrl, Long userId);

// IMPLEMENTATION: MISSING - must implement
```

#### Wrong Return Type
```java
// INTERFACE (ITestCaseService.java:269)
String exportTestCases(Long projectId, List<Long> testCaseIds, String format);

// IMPLEMENTATION (TestCaseServiceImpl.java:130)
byte[] exportTestCases(...) // ❌ WRONG - returns byte[] instead of String
```

#### Non-Existent IProjectService Methods
```java
// TestCaseServiceImpl.java calls methods that DON'T EXIST in IProjectService:
projectService.hasProjectAccess(projectId, userId);              // Line 184 ❌
projectService.hasProjectEditPermission(projectId, userId);      // Line 193 ❌
projectService.hasProjectDeletePermission(projectId, userId);    // Line 202 ❌

// SOLUTION OPTIONS:
// A. Remove these permission checks (risky)
// B. Use existing methods: isProjectMember(), isProjectAdmin()
// C. Add these methods to IProjectService interface
```

#### @Override Mismatches (12 errors)
```java
// Lines with @Override errors: 70, 83, 89, 94, 99, 106, 112, 117, 122, 129, 150
// All methods have wrong signatures compared to interface
```

**Fix Strategy**:
1. Change `exportTestCases` return type: `byte[]` → `String` (file URL/path)
2. Implement missing `importTestCases` method
3. Replace non-existent permission methods with `isProjectMember()`
4. Fix all @Override signature mismatches

---

### 2. DocumentServiceImpl.java (28 errors)

**Problem**: Missing abstract method

#### Missing delete() Method
```java
// INTERFACE (IDocumentService.java:140)
void delete(Long id);

// IMPLEMENTATION: MISSING - only has deleteDocument(Long, Long)
```

#### @Override Mismatches (2+ errors)
```java
// LINES: 139, 264
// Methods don't match interface signatures
```

#### Symbol Not Found (25+ errors)
```java
// Multiple "找不到符号" errors suggesting:
// 1. Missing DTO fields
// 2. Wrong method calls on service dependencies
// 3. Type mismatches
```

**Fix Strategy**:
1. Add `delete(Long id)` method that calls `deleteDocument(id, getCurrentUserId())`
2. Audit and fix all @Override methods
3. Identify missing DTO fields causing symbol errors

---

### 3. TaskServiceImpl.java (10 errors)

**Problem**: Missing abstract method

#### Missing batchUpdateTasks() Method
```java
// INTERFACE (ITaskService.java:314-320)
int batchUpdateTasks(
    List<Long> taskIds,
    Integer status,
    Integer priority,
    Long assigneeId,
    String tags,
    Long userId);

// IMPLEMENTATION: MISSING - must implement
```

#### @Override Mismatches (4 errors)
```java
// LINES: 105, 113, 119, 227
// Method signatures don't match interface
```

**Fix Strategy**:
1. Implement `batchUpdateTasks()` method
2. Fix @Override signature mismatches

---

### 4. ProjectServiceImpl.java (20 errors)

**Problem**: Missing abstract method + type conversions

#### Missing listUserProjects() Method
```java
// INTERFACE (IProjectService.java:82-86)
PageResult<Project> listUserProjects(
    Long userId, Integer page, Integer pageSize, Integer status);

// IMPLEMENTATION: MISSING or wrong signature
```

#### String→Long Type Conversion Errors (3 errors)
```java
// LINES: 74, 111, 133
// Passing String where Long expected
```

#### @Override Mismatches (6+ errors)
```java
// LINES: 80, 160, 166, 184, 190, 202
// Method signatures don't match interface
```

**Fix Strategy**:
1. Implement missing `listUserProjects()` variations
2. Fix String→Long conversions (likely SecurityUtils.getCurrentUserId())
3. Fix @Override signature mismatches

---

### 5. OrganizationServiceImpl.java (12 errors)

**Problem**: Missing abstract method

#### Missing listOrganizationMembers() Method
```java
// INTERFACE (IOrganizationService - need to read)
PageResult<OrganizationMemberDTO> listOrganizationMembers(
    Long organizationId, Long requesterId, Integer page, Integer pageSize);

// IMPLEMENTATION: MISSING or wrong signature
```

#### @Override Mismatches (6 errors)
```java
// LINES: 144, 173, 182, 188, 194
// Method signatures don't match interface
```

**Fix Strategy**:
1. Read IOrganizationService interface
2. Implement missing methods
3. Fix @Override mismatches

---

## 🟢 Medium Priority Issues

### Strategy Classes - Symbol Not Found Errors

#### 1. ChangeRequestApprovalStrategy.java (12 errors)
- Missing fields/methods in related entities
- Likely ChangeRequestApproval entity field mismatches

#### 2. DocumentUploadStrategy.java (10 errors)
```java
// Missing method (Line 77):
downloadInfo.setDownloadUrl(url); // ❌ setDownloadUrl() doesn't exist

// Likely fix: Check DocumentDownloadInfo DTO for correct field name
```

#### 3. DocumentApplicationServiceImpl.java (14 errors)
- Multiple symbol not found errors
- Need to audit entity/DTO field names

#### 4. DocumentFavoriteStrategy.java (6 errors)
- Lambda expression issues (likely similar to ProjectStatsStrategy)
- Symbol not found errors

#### 5. ProjectMemberStrategy.java (4 errors)
```java
// Missing methods (Lines 86, 195):
roleService.getRoleByName(roleName);  // ❌ doesn't exist
dto.setRole(roleName);                // ❌ wrong field name
```

---

## Fix Execution Plan

### Batch 1: Interface Method Implementations (Priority 1)
**Objective**: Add all missing abstract methods

| File | Missing Method | Estimated Effort |
|------|----------------|------------------|
| TestCaseServiceImpl | importTestCases() | 30 min |
| TaskServiceImpl | batchUpdateTasks() | 20 min |
| ProjectServiceImpl | listUserProjects() | 20 min |
| OrganizationServiceImpl | listOrganizationMembers() | 20 min |
| DocumentServiceImpl | delete(Long) | 10 min |

**Risk**: Low - adding methods won't break existing code

---

### Batch 2: Return Type Corrections (Priority 1)
**Objective**: Fix critical return type mismatches

| File | Method | Change Required |
|------|--------|-----------------|
| TestCaseServiceImpl | exportTestCases() | byte[] → String |

**Risk**: Medium - need to change implementation logic to return file URL instead of bytes

---

### Batch 3: Non-Existent Method Replacements (Priority 1)
**Objective**: Replace calls to non-existent methods

| File | Non-Existent Method | Replacement |
|------|---------------------|-------------|
| TestCaseServiceImpl | hasProjectAccess() | isProjectMember() |
| TestCaseServiceImpl | hasProjectEditPermission() | isProjectAdmin() |
| TestCaseServiceImpl | hasProjectDeletePermission() | isProjectAdmin() |

**Risk**: Low - straightforward replacements

---

### Batch 4: @Override Signature Fixes (Priority 2)
**Objective**: Fix method signatures to match interfaces

**Strategy**: For each file, read interface and implementation side-by-side, fix signatures one method at a time

**Files**: TestCaseServiceImpl (12), DocumentServiceImpl (2), TaskServiceImpl (4), ProjectServiceImpl (6), OrganizationServiceImpl (6)

**Risk**: Medium - may expose additional cascading errors

---

### Batch 5: DTO Field Audit (Priority 2)
**Objective**: Identify and add missing DTO fields

**Approach**:
1. Collect all "symbol not found" errors
2. Map to DTO classes
3. Verify correct field names in DTOs
4. Add missing fields or fix field name references

**Risk**: Medium - DTO changes may affect API layer

---

### Batch 6: Type Conversion Fixes (Priority 3)
**Objective**: Fix String→Long and other type mismatches

**Files**: ProjectServiceImpl (3 errors), possibly others

**Risk**: Low - straightforward type fixes

---

## Validation Strategy

After each batch:
1. ✅ Compile affected module only: `mvn compile -pl promanage-service -DskipTests`
2. ✅ Check error count reduction
3. ✅ Verify no new errors introduced
4. ✅ Document any new issues discovered

After all batches:
1. ✅ Full backend compile: `mvn clean compile -DskipTests`
2. ✅ Run unit tests: `mvn test -Dcheckstyle.skip=true`
3. ✅ Integration test spot check
4. ✅ Create final implementation summary

---

## Risk Assessment

### Low Risk Fixes (Do First)
- ✅ Add missing abstract methods with stub implementations
- ✅ Replace non-existent method calls
- ✅ Type conversion fixes

### Medium Risk Fixes (Do Second)
- ⚠️ @Override signature fixes (may expose more errors)
- ⚠️ Return type changes (requires logic changes)
- ⚠️ DTO field additions (may affect API contracts)

### High Risk Fixes (Do Last / Consider Deferring)
- 🔴 Major refactoring of service implementations
- 🔴 Breaking API contract changes
- 🔴 Database schema changes

---

## Next Steps

**Recommended Execution Order**:

1. **Batch 1**: Add missing methods (stubs OK) → Reduce errors by ~5
2. **Batch 3**: Replace non-existent methods → Reduce errors by ~6
3. **Batch 6**: Type conversions → Reduce errors by ~3
4. **Compile & Assess**: Check if error count drops significantly
5. **Batch 2**: Fix return types → Reduce errors by ~1
6. **Batch 4**: @Override fixes → Reduce errors by ~30
7. **Batch 5**: DTO fields → Reduce errors by ~25+
8. **Final Validation**: Full compile and test

**Estimated Timeline**: 4-6 hours of focused work

**Success Criteria**: Backend compiles with 0 errors, core tests pass

---

## Appendix: Detailed Error Logs

### TestCaseServiceImpl Detailed Errors
```
[ERROR] Line 38: class is not abstract and does not override abstract method importTestCases(Long,String,Long)
[ERROR] Line 70: method does not override or implement a method from a supertype
[ERROR] Line 83: method does not override or implement a method from a supertype
[ERROR] Line 89: method does not override or implement a method from a supertype
[ERROR] Line 94: method does not override or implement a method from a supertype
[ERROR] Line 99: method does not override or implement a method from a supertype
[ERROR] Line 106: method does not override or implement a method from a supertype
[ERROR] Line 112: method does not override or implement a method from a supertype
[ERROR] Line 117: method does not override or implement a method from a supertype
[ERROR] Line 122: method does not override or implement a method from a supertype
[ERROR] Line 129: method does not override or implement a method from a supertype
[ERROR] Line 130: return type byte[] is incompatible with String
[ERROR] Line 150: method does not override or implement a method from a supertype
[ERROR] Lines 184, 193, 202, 211, 216, 221: cannot find symbol - hasProjectAccess/hasProjectEditPermission/hasProjectDeletePermission
```

### Symbol Resolution Map
```
DocumentDownloadInfo:
  ❌ setDownloadUrl()
  ✅ Need to check actual field name

IRoleService:
  ❌ getRoleByName()
  ✅ Need to check actual method name

ProjectMemberDTO:
  ❌ setRole()
  ✅ Need to check actual field name
```

---

**End of Audit Report**
