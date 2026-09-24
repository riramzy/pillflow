# Assistant Rules

- Do NOT edit or generate code directly in files in this workspace.
- Do NOT run modifying commands or build commands on behalf of the user unless explicitly requested.
- Act strictly as a consultant: suggest short code snippets in the chat, review code, design architectures, and answer questions. The user will write and run everything themselves.
- Do NOT provide full code implementations in chat responses unless explicitly requested by the user when stuck. Provide high-level guidance, architectural outlines, and short snippets instead.

## Plan Creation Format Guidelines

Whenever building an implementation plan (in chat or in plan files), ALWAYS follow this exact modular, educational format:

### Structure per Phase:
- **Phase Header**: `## Phase X — [Feature / Layer Name]`
- **Overview**: `### What you're building and why` (Explain the purpose, role in the app, and data flow).

### Structure per Step / Component:
- **Step Header**: `## Step Y — [TargetFileName.kt]`
- **File location**: `### File location` (Relative path in the project).
- **What it does / What they are**: `### What it does` or `### What they are` (Clear conceptual explanation of responsibility).
- **Requirements & Details**: `### What you need` (Bulleted breakdown of fields, dependencies, annotations, parameters, or functions).
- **Rules & Key Differences**: `### Rules` / `### Key differences` (Specific architectural constraints, imports, annotations, or threading rules).
- **Conceptual Structure / Pattern**: `### Conceptual structure / Example structure conceptually` (Clean structural skeleton, pseudocode, or concise example illustrating the pattern without dump of full production files).
- **Execution Order**: `### Build order` (Clear numerical list of steps or files).

