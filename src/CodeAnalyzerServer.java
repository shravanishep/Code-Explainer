import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CodeAnalyzerServer {

    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        
        server.createContext("/", new StaticFileHandler());
        server.createContext("/analyze", new AnalyzeHandler());
        
        server.setExecutor(null);
        server.start();
        
        System.out.println("========================================");
        System.out.println("  Code Analyzer Server Started!");
        System.out.println("========================================");
        System.out.println("Open your browser and navigate to:");
        System.out.println("  http://localhost:" + port);
        System.out.println("========================================");
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String html = getIndexHTML();
            byte[] response = html.getBytes(StandardCharsets.UTF_8);
            
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.length);
            
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        }
    }

    static class AnalyzeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr);
                StringBuilder code = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    code.append(line).append("\n");
                }
                
                String result = analyzeCode(code.toString());
                byte[] response = result.getBytes(StandardCharsets.UTF_8);
                
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, response.length);
                
                OutputStream os = exchange.getResponseBody();
                os.write(response);
                os.close();
            }
        }
    }

    private static String analyzeCode(String code) {
        try {
            CompilationUnit cu = StaticJavaParser.parse(code);
            ComplexityVisitor visitor = new ComplexityVisitor();
            visitor.visit(cu, null);
            
            StringBuilder json = new StringBuilder();
            json.append("{\"success\": true, \"results\": [");
            
            boolean first = true;
            for (MethodReport r : visitor.getReports()) {
                if (!first) json.append(",");
                first = false;
                
                json.append("{");
                json.append("\"method\": \"").append(escapeJson(r.name)).append("\",");
                json.append("\"timeComplexity\": \"").append(estimateTime(r)).append("\",");
                json.append("\"spaceComplexity\": \"").append(r.isRecursive ? "O(n)" : "O(1)").append("\",");
                json.append("\"isRecursive\": ").append(r.isRecursive).append(",");
                json.append("\"nestedDepth\": ").append(r.maxNestedLoopDepth).append(",");
                json.append("\"loops\": \"").append(r.loops.toString()).append("\"");
                json.append("}");
            }
            
            json.append("]}");
            return json.toString();
            
        } catch (Exception e) {
            return "{\"success\": false, \"error\": \"" + escapeJson(e.getMessage()) + "\"}";
        }
    }

    private static String estimateTime(MethodReport r) {
        if (r.loops.isEmpty() && !r.isRecursive) return "O(1)";
        if (r.loops.isEmpty()) return "O(n)";

        // Only multiply loops up to the max nesting depth
        StringBuilder sb = new StringBuilder("O(");
        boolean first = true;
        
        int nestedCount = 0;
        for (LoopGrowth g : r.loops) {
            nestedCount++;
            if (nestedCount > r.maxNestedLoopDepth) {
                // Sequential loop - add instead of multiply
                sb.append(" + ");
            } else if (!first) {
                // Nested loop - multiply
                sb.append(" * ");
            }
            first = false;

            switch (g) {
                case CONSTANT -> sb.append("1");
                case LINEAR -> sb.append("n");
                case LOGARITHMIC -> sb.append("log n");
                default -> sb.append("?");
            }
        }

        sb.append(")");
        return sb.toString();
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static String getIndexHTML() {
        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Code Complexity Analyzer</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 20px;
        }

        .container {
            max-width: 1200px;
            margin: 0 auto;
        }

        header {
            text-align: center;
            color: white;
            margin-bottom: 30px;
        }

        header h1 {
            font-size: 2.5em;
            margin-bottom: 10px;
            text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
        }

        header p {
            font-size: 1.1em;
            opacity: 0.9;
        }

        .content {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
            margin-bottom: 20px;
        }

        .panel {
            background: white;
            border-radius: 10px;
            padding: 25px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.2);
        }

        .panel h2 {
            color: #667eea;
            margin-bottom: 15px;
            font-size: 1.5em;
        }

        textarea {
            width: 100%;
            height: 400px;
            padding: 15px;
            border: 2px solid #e0e0e0;
            border-radius: 8px;
            font-family: 'Courier New', monospace;
            font-size: 14px;
            resize: vertical;
            transition: border-color 0.3s;
        }

        textarea:focus {
            outline: none;
            border-color: #667eea;
        }

        .button-group {
            display: flex;
            gap: 10px;
            margin-top: 15px;
        }

        button {
            flex: 1;
            padding: 12px 24px;
            border: none;
            border-radius: 8px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s;
        }

        .btn-primary {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
        }

        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
        }

        .btn-secondary {
            background: #f0f0f0;
            color: #333;
        }

        .btn-secondary:hover {
            background: #e0e0e0;
        }

        .file-upload {
            margin-bottom: 15px;
        }

        .file-upload input[type="file"] {
            display: none;
        }

        .file-upload label {
            display: inline-block;
            padding: 10px 20px;
            background: #f8f8f8;
            border: 2px dashed #ccc;
            border-radius: 8px;
            cursor: pointer;
            transition: all 0.3s;
        }

        .file-upload label:hover {
            background: #f0f0f0;
            border-color: #667eea;
        }

        #results {
            min-height: 400px;
        }

        .method-card {
            background: #f8f9fa;
            border-left: 4px solid #667eea;
            padding: 15px;
            margin-bottom: 15px;
            border-radius: 5px;
        }

        .method-card h3 {
            color: #333;
            margin-bottom: 10px;
            font-size: 1.2em;
        }

        .complexity-info {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 10px;
            margin-bottom: 10px;
        }

        .complexity-badge {
            padding: 8px 12px;
            border-radius: 5px;
            font-weight: 600;
            text-align: center;
        }

        .time-complexity {
            background: #e3f2fd;
            color: #1976d2;
        }

        .space-complexity {
            background: #f3e5f5;
            color: #7b1fa2;
        }

        .recursive-badge {
            background: #fff3e0;
            color: #f57c00;
            padding: 5px 10px;
            border-radius: 5px;
            display: inline-block;
            margin-top: 5px;
            font-size: 0.9em;
        }

        .error {
            background: #ffebee;
            color: #c62828;
            padding: 15px;
            border-radius: 5px;
            border-left: 4px solid #c62828;
        }

        .empty-state {
            text-align: center;
            color: #999;
            padding: 50px;
        }

        .empty-state i {
            font-size: 3em;
            margin-bottom: 15px;
        }

        @media (max-width: 768px) {
            .content {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <header>
            <h1>🔍 Code Complexity Analyzer</h1>
            <p>Upload or paste Java code to analyze time and space complexity</p>
        </header>

        <div class="content">
            <div class="panel">
                <h2>📝 Input Code</h2>
                <div class="file-upload">
                    <label for="fileInput">📁 Choose Java File</label>
                    <input type="file" id="fileInput" accept=".java">
                    <span id="fileName" style="margin-left: 10px; color: #666;"></span>
                </div>
                <textarea id="codeInput" placeholder="Paste your Java code here or upload a file...">public class Example {
    
    static int factorial(int n) {
        if (n == 0) return 1;
        return n * factorial(n - 1);
    }
    
    static void nestedLoop(int n) {
        for(int i = 0; i < n; i++) {
            for(int j = 1; j < n; j *= 2) {
                System.out.println(i + j);
            }
        }
    }
    
    public static void main(String[] args) {
        System.out.println(factorial(5));
        nestedLoop(10);
    }
}</textarea>
                <div class="button-group">
                    <button class="btn-primary" onclick="analyzeCode()">🚀 Analyze Code</button>
                    <button class="btn-secondary" onclick="clearCode()">🗑️ Clear</button>
                </div>
            </div>

            <div class="panel">
                <h2>📊 Analysis Results</h2>
                <div id="results">
                    <div class="empty-state">
                        <div style="font-size: 3em;">📈</div>
                        <p>Upload or paste code and click "Analyze Code" to see results</p>
                    </div>
                </div>
            </div>
        </div>

        <div class="panel">
            <h2>✨ Features</h2>
            <p style="color: #666; line-height: 1.6;">
                ✓ Detects direct and mutual recursion<br>
                ✓ Analyzes all loop types (for, foreach, while, do-while)<br>
                ✓ Identifies linear, logarithmic, and constant growth patterns<br>
                ✓ Provides worst-case complexity estimates
            </p>
        </div>
    </div>

    <script>
        document.getElementById('fileInput').addEventListener('change', function(e) {
            const file = e.target.files[0];
            if (file) {
                document.getElementById('fileName').textContent = file.name;
                const reader = new FileReader();
                reader.onload = function(e) {
                    document.getElementById('codeInput').value = e.target.result;
                };
                reader.readAsText(file);
            }
        });

        async function analyzeCode() {
            const code = document.getElementById('codeInput').value.trim();
            const resultsDiv = document.getElementById('results');
            
            if (!code) {
                resultsDiv.innerHTML = '<div class="error">Please enter some Java code to analyze.</div>';
                return;
            }

            resultsDiv.innerHTML = '<div style="text-align:center;padding:50px;color:#667eea;">Analyzing code...</div>';

            try {
                const response = await fetch('/analyze', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'text/plain'
                    },
                    body: code
                });

                const data = await response.json();

                if (data.success) {
                    displayResults(data.results);
                } else {
                    resultsDiv.innerHTML = `<div class="error"><strong>Error:</strong> ${data.error}</div>`;
                }
            } catch (error) {
                resultsDiv.innerHTML = `<div class="error"><strong>Error:</strong> ${error.message}</div>`;
            }
        }

        function displayResults(results) {
            const resultsDiv = document.getElementById('results');
            
            if (results.length === 0) {
                resultsDiv.innerHTML = '<div class="empty-state"><p>No methods found in the code.</p></div>';
                return;
            }

            let html = '';
            results.forEach(result => {
                html += `
                    <div class="method-card">
                        <h3>Method: ${result.method}</h3>
                        <div class="complexity-info">
                            <div class="complexity-badge time-complexity">
                                ⏱️ Time: ${result.timeComplexity}
                            </div>
                            <div class="complexity-badge space-complexity">
                                💾 Space: ${result.spaceComplexity}
                            </div>
                        </div>
                        ${result.isRecursive ? '<span class="recursive-badge">🔄 Recursive</span>' : ''}
                        ${result.loops !== '[]' ? `<div style="margin-top:10px;color:#666;font-size:0.9em;">Loops: ${result.loops}</div>` : ''}
                    </div>
                `;
            });

            resultsDiv.innerHTML = html;
        }

        function clearCode() {
            document.getElementById('codeInput').value = '';
            document.getElementById('fileInput').value = '';
            document.getElementById('fileName').textContent = '';
            document.getElementById('results').innerHTML = `
                <div class="empty-state">
                    <div style="font-size: 3em;">📈</div>
                    <p>Upload or paste code and click "Analyze Code" to see results</p>
                </div>
            `;
        }
    </script>
</body>
</html>
        """;
    }
}
