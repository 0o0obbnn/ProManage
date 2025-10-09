# Page snapshot

```yaml
- generic [ref=e4]:
  - generic [ref=e5]:
    - img "ProManage" [ref=e6]
    - heading "ProManage" [level=1] [ref=e7]
    - paragraph [ref=e8]: 智能项目管理系统
  - generic [ref=e9]:
    - generic [ref=e15]:
      - img "user" [ref=e17]:
        - img [ref=e18]
      - textbox "用户名" [ref=e20]
    - generic [ref=e26]:
      - img "lock" [ref=e28]:
        - img [ref=e29]
      - textbox "密码" [ref=e31]
      - img "eye-invisible" [ref=e33] [cursor=pointer]:
        - img [ref=e34] [cursor=pointer]
    - generic [ref=e42]:
      - generic [ref=e43] [cursor=pointer]:
        - checkbox "记住我" [ref=e45] [cursor=pointer]
        - generic [ref=e47] [cursor=pointer]: 记住我
      - link "忘记密码?" [ref=e48] [cursor=pointer]:
        - /url: /forgot-password
    - button "登 录" [ref=e54] [cursor=pointer]:
      - generic [ref=e55] [cursor=pointer]: 登 录
    - generic [ref=e57]:
      - text: 还没有账号?
      - link "立即注册" [ref=e58] [cursor=pointer]:
        - /url: /register
```