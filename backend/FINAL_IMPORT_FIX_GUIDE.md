# Final Import Fix Guide - Complete Solution

## Summary

**Status**: Partially fixed (7/15 files completed)
**Remaining**: 8 files with import errors + missing mapper classes

## ✅ Successfully Fixed Files

1. DocumentServiceImpl.java
2. DocumentQueryStrategy.java
3. DocumentUploadStrategy.java
4. DocumentFavoriteStrategy.java
5. DocumentVersionStrategy.java
6. OrganizationServiceImpl.java
7. OrganizationQueryStrategy.java

## ❌ Remaining Issues

### Issue 1: Missing Mapper Classes
The following mappers are referenced but don't exist:
- `OrganizationMemberMapper` - Expected at: `com.promanage.service.mapper.OrganizationMemberMapper`
- `OrganizationSettingsMapper` - Expected at: `com.promanage.service.mapper.OrganizationSettingsMapper`

**Solution**: These mappers need to be created OR the strategy classes need to be refactored to not use them.

### Issue 2: Incorrect Imports - Need Fixing

#### Files with `ResultCode` import errors:
Change `com.promanage.common.result.ResultCode` → `com.promanage.common.domain.ResultCode`
OR
Change `com.promanage.common.enums.ResultCode` → `com.promanage.common.domain.ResultCode`

1. ProjectMemberStrategy.java (line 11)
2. TaskServiceImpl.java (line 12)
3. TaskCommentStrategy.java (line 11)
4. TaskAttachmentStrategy.java (line 8)
5. TaskCheckItemStrategy.java (line 8)
6. UserServiceImpl.java (line 13)
7. UserAuthStrategy.java (line 9)
8. UserProfileStrategy.java (line 9)

#### Files with `User` entity import errors:
Change `com.promanage.service.entity.User` → `com.promanage.common.entity.User`

1. UserServiceImpl.java (line 16)
2. UserQueryStrategy.java (line 9)
3. UserAuthStrategy.java (line 11)
4. UserProfileStrategy.java (line 11)

#### Files with `Project` entity/mapper import errors:
- Change `com.promanage.domain.entity.Project` → `com.promanage.common.entity.Project`
- Change `com.promanage.domain.mapper.ProjectMapper` → `com.promanage.service.mapper.ProjectMapper`
- Change `com.promanage.service.dto.*` → `com.promanage.dto.*`

1. ProjectServiceImpl.java (lines 12-25)

## Quick Fix Commands

### Fix ResultCode imports (8 files):
```bash
# ProjectMemberStrategy.java
sed -i 's/com.promanage.common.result.ResultCode/com.promanage.common.domain.ResultCode/g' F:/projects/ProManage/backend/promanage-service/src/main/java/com/promanage/service/strategy/ProjectMemberStrategy.java

# Task-related files
sed -i 's/com.promanage.common.result.ResultCode/com.promanage.common.domain.ResultCode/g' F:/projects/ProManage/backend/promanage-service/src/main/java/com/promanage/service/impl/TaskServiceImpl.java
sed -i 's/com.promanage.common.result.ResultCode/com.promanage.common.domain.ResultCode/g' F:/projects/ProManage/backend/promanage-service/src/main/java/com/promanage/service/strategy/TaskCommentStrategy.java
sed -i 's/com.promanage.common.result.ResultCode/com.promanage.common.domain.ResultCode/g' F:/projects/ProManage/backend/promanage-service/src/main/java/com/promanage/service/strategy/TaskAttachmentStrategy.java
sed -i 's/com.promanage.common.result.ResultCode/com.promanage.common.domain.ResultCode/g' F:/projects/ProManage/backend/promanage-service/src/main/java/com/promanage/service/strategy/TaskCheckItemStrategy.java

# User-related files
sed -i 's/com.promanage.common.result.ResultCode/com.promanage.common.domain.ResultCode/g' F:/projects/ProManage/backend/promanage-service/src/main/java/com/promanage/service/impl/UserServiceImpl.java
sed -i 's/com.promanage.common.result.ResultCode/com.promanage.common.domain.ResultCode/g' F:/projects/ProManage/backend/promanage-service/src/main/java/com/promanage/service/strategy/UserAuthStrategy.java
sed -i 's/com.promanage.common.result.ResultCode/com.promanage.common.domain.ResultCode/g' F:/projects/ProManage/backend/promanage-service/src/main/java/com/promanage/service/strategy/UserProfileStrategy.java
```

### Fix User entity imports (4 files):
```bash
sed -i 's/com.promanage.service.entity.User/com.promanage.common.entity.User/g' F:/projects/ProManage/backend/promanage-service/src/main/java/com/promanage/service/impl/UserServiceImpl.java
sed -i 's/com.promanage.service.entity.User/com.promanage.common.entity.User/g' F:/projects/ProManage/backend/promanage-service/src/main/java/com/promanage/service/strategy/UserQueryStrategy.java
sed -i 's/com.promanage.service.entity.User/com.promanage.common.entity.User/g' F:/projects/ProManage/backend/promanage-service/src/main/java/com/promanage/service/strategy/UserAuthStrategy.java
sed -i 's/com.promanage.service.entity.User/com.promanage.common.entity.User/g' F:/projects/ProManage/backend/promanage-service/src/main/java/com/promanage/service/strategy/UserProfileStrategy.java
```

## Critical Blocker: Missing Mappers

The OrganizationMemberStrategy and OrganizationSettingsStrategy reference mappers that don't exist:
- OrganizationMemberMapper
- OrganizationSettingsMapper

**Options**:
1. Create these mapper classes
2. Refactor the strategy classes to use direct repository/DAO access
3. Comment out the problematic code temporarily

## After Fixes

Once all imports are corrected:
```bash
cd F:\projects\ProManage\backend\promanage-service
mvn clean compile -DskipTests -Dcheckstyle.skip=true
```

Then rebuild promanage-api:
```bash
cd F:\projects\ProManage\backend\promanage-api
mvn clean compile -DskipTests -Dcheckstyle.skip=true
```

## Impact on ChangeRequestController

Once promanage-service compiles successfully:
- ✅ ChangeRequestController errors will automatically resolve
- ✅ No changes needed to ChangeRequestController itself
- ✅ The imports in ChangeRequestController are already correct

---
**Priority**: HIGH - Blocks entire backend compilation
**Estimated Time**: 15-30 minutes to complete all fixes
