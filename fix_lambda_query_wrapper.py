#!/usr/bin/env python3
"""
修复 ProjectServiceImplTest.java 中的 LambdaQueryWrapper 类型安全问题
为所有使用 any(LambdaQueryWrapper.class) 的测试方法添加 @SuppressWarnings("unchecked") 注解
"""

import re

def fix_test_file():
    file_path = r"G:\nifa\ProManage\backend\promanage-service\src\test\java\com\promanage\service\impl\ProjectServiceImplTest.java"
    
    # 读取文件内容
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # 使用正则表达式找到所有需要修复的测试方法
    # 匹配模式：@Test 后跟着 @DisplayName，然后是 void 方法名，方法体内包含 any(LambdaQueryWrapper.class)
    pattern = r'(@Test\s+\n\s+@DisplayName\("[^"]*"\)\s+\n\s+)(void\s+\w+\([^)]*\)\s*\{[^}]*any\(LambdaQueryWrapper\.class\)[^}]*\})'
    
    def replace_func(match):
        # 在 @Test 和 @DisplayName 之间添加 @SuppressWarnings("unchecked")
        return match.group(1) + '@SuppressWarnings("unchecked")\n    ' + match.group(2)
    
    # 应用替换
    fixed_content = re.sub(pattern, replace_func, content, flags=re.MULTILINE | re.DOTALL)
    
    # 写回文件
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(fixed_content)
    
    print("修复完成！")

if __name__ == "__main__":
    fix_test_file()