import java.io.*;
import java.nio.file.*;
import java.util.*;

class FixDuplicateMethods {
    public static void main(String[] args) throws IOException {
        String filePath = "G:\\nifa\\ProManage\\backend\\promanage-service\\src\\main\\java\\com\\promanage\\service\\impl\\ProjectServiceImpl.java";
        
        // 读取文件内容
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        
        // 查找重复的方法定义
        List<Integer> methodLines = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.contains("@Override") && line.contains("public Project getProjectById")) {
                methodLines.add(i);
            }
        }
        
        // 如果找到多个方法定义，删除多余的
        if (methodLines.size() > 1) {
            // 保留第一个，删除其余的
            for (int i = methodLines.size() - 1; i > 0; i--) {
                int startLine = methodLines.get(i);
                // 查找方法结束位置
                int endLine = startLine;
                while (endLine < lines.size() && !lines.get(endLine).contains("}")) {
                    endLine++;
                }
                if (endLine < lines.size()) {
                    endLine++; // 包含右大括号
                }
                
                // 删除方法定义
                for (int j = endLine - 1; j >= startLine; j--) {
                    lines.remove(j);
                }
            }
            
            // 写回文件
            Files.write(Paths.get(filePath), lines);
            System.out.println("修复完成，删除了重复的方法定义");
        } else {
            System.out.println("未找到重复的方法定义");
        }
    }
}