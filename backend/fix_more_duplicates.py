import re

# 读取文件
with open('promanage-service/src/main/java/com/promanage/service/impl/DocumentServiceImpl.java', 'r', encoding='utf-8') as f:
    lines = f.readlines()

# 删除重复的方法：保留后面的实现，删除前面的
# 删除行233-242的searchByKeyword (保留446行的)
# 删除行392-409的getByIdWithoutView (保留后面的)
# 删除行368-378的delete (保留后面的)
# 删除行457-467的getDocumentFolders (保留841行的)

# 标记要删除的行范围
to_delete = []
to_delete.extend(range(232, 242))  # searchByKeyword第一个 (233-242, 0-indexed: 232-241)
to_delete.extend(range(367, 378))  # delete第一个 (368-378)
to_delete.extend(range(391, 410))  # getByIdWithoutView第一个 (392-409)
to_delete.extend(range(456, 468))  # getDocumentFolders第一个 (457-467)

# 创建新的行列表，跳过要删除的行
new_lines = [line for i, line in enumerate(lines) if i not in to_delete]

# 写回文件
with open('promanage-service/src/main/java/com/promanage/service/impl/DocumentServiceImpl.java', 'w', encoding='utf-8') as f:
    f.writelines(new_lines)

print(f"Deleted {len(to_delete)} duplicate lines")
