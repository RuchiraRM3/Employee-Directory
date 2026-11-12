## Agents
- planner: system design, PRD review, technical specs, task breakdown
- implementer: writing production code, building features from plans, tests
- reviewer: code review, QA, security checks, spec verification
- researcher: codebase exploration, pattern tracing, pre-implementation context
- devops: Docker, CI/CD pipelines, GCP Cloud Run, infrastructure

## Workflow
- PRD → Plan → Implement → Review is the standard pipeline for non-trivial work
- `/create-prd` → planner agent → implementer agent → reviewer agent
- Use `cc-todos` to track in-session task lists
- Use `cc-create-plan` for lightweight plans that don't need a full PRD

## Rules (Auto-loaded)
- Rules in .claude/rules/ apply automatically by file path:
  angular.md, react.md, node.md, spring.md, docker.md, database.md, security.md, cicd.md

# Karpathy Coding Principles

Behavioral guidelines to reduce common LLM coding mistakes.
Source: github.com/forrestchang/andrej-karpathy-skills

## 1. Think Before Coding
- State assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them. Don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.

## 2. Simplicity First
- No features beyond what was asked.
- No abstractions for single-use code.
- No error handling for impossible scenarios.
- If 200 lines could be 50, rewrite it.

## 3. Surgical Changes
- Touch only what you must. Match existing style.
- Don't refactor things that aren't broken.
- Remove only the imports/vars your own change made unused.

## 4. Goal-Driven Execution
- Turn tasks into verifiable goals ("fix bug" = "write failing test, make it pass").
- For multi-step tasks, state a short plan with a verify step for each.

# Plugin Usage
The superpowers plugin is the default skill harness. If a skill might apply,
invoke it via the Skill tool before responding.

| Principle            | Skill(s)                                                        |
|----------------------|-----------------------------------------------------------------|
| Think Before Coding  | superpowers:brainstorming                                       |
| Simplicity First     | simplify, superpowers:receiving-code-review                     |
| Surgical Changes     | superpowers:writing-plans, executing-plans, using-git-worktrees |
| Goal-Driven          | superpowers:test-driven-development,                            |
|                      | verification-before-completion, systematic-debugging            |

## Enforcement order
1. User's explicit instructions
2. Karpathy principles
3. Superpowers skill content
4. Default harness behavior