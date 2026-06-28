
import os

def resolve_file(filepath):
    try:
        with open(filepath, "r", encoding="utf-8") as f:
            content = f.read()
    except Exception as e:
        return
        
    if "<<<<<<< HEAD" not in content:
        return
        
    lines = content.split("\n")
    new_lines = []
    in_conflict = False
    
    for line in lines:
        if line.startswith("<<<<<<< HEAD"):
            in_conflict = True
        elif line.startswith("======="):
            pass
        elif line.startswith(">>>>>>> "):
            in_conflict = False
        else:
            new_lines.append(line)
            
    with open(filepath, "w", encoding="utf-8") as f:
        f.write("\n".join(new_lines))

for root, _, files in os.walk("."):
    for file in files:
        if file.endswith(".java") or file.endswith(".xml") or file.endswith(".gradle") or file.endswith(".toml"):
            resolve_file(os.path.join(root, file))

