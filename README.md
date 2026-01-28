# Code-Explainer
Code Explainer is a hybrid static analysis and AI-assisted tool that explains code behavior, detects logical issues, and estimates time and space complexity. It uses AST-based analysis and rule-driven validation before generating structured, interview-focused explanations.

## Features
✓ Detects direct and mutual recursion  
✓ Analyzes all loop types (for, foreach, while, do-while)  
✓ Identifies linear, logarithmic, and constant growth patterns  
✓ Provides worst-case complexity estimates  
✓ Web-based UI for easy code analysis

## How to Run

### Web UI (Recommended)
```bash
java CodeAnalyzerServer
```
Then open your browser to `http://localhost:8080`

### Command Line
```bash
java Main
```

## Usage
1. Start the web server using `CodeAnalyzerServer`
2. Open the browser interface
3. Upload a Java file or paste code directly
4. Click "Analyze Code" to see complexity analysis
5. View time/space complexity for each method
