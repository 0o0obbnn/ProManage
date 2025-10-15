import re

# 读取文件
with open('promanage-service/src/main/java/com/promanage/service/impl/DocumentServiceImpl.java', 'r', encoding='utf-8') as f:
    content = f.read()

# 删除109-210行之间的重复方法（第一组重复）
lines = content.split('\n')
# 保留1-108行和211行之后的内容
new_lines = lines[:108] + lines[210:]
content = '\n'.join(new_lines)

# 修复PageResult.of参数类型
content = content.replace(
    '                (int) pageResult.getCurrent(),\n                (int) pageResult.getSize()',
    '                pageResult.getCurrent(),\n                pageResult.getSize()'
)

# 写回文件
with open('promanage-service/src/main/java/com/promanage/service/impl/DocumentServiceImpl.java', 'w', encoding='utf-8') as f:
    f.write(content)

print("Fixed duplicates successfully")
