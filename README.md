# rust-analyzer
An Analyzer for Rust Codebases. Determine things like, what is the global Rust-Lang UNSAFE space, etc. What is the set of Rust Features employed (utilizing Rust-Features project) etc.

   Design Concepts:

   1. Parse/Gen a semantic model of a Rust Static + Runtime representation (Static + Occurence-Count + Time Signatures)
      1.0. "Static"
      1.1. Occurence-Count
      1.2. Time Signatures
      1.1. "Code-Time" == Static  + (OccCn * TimeAvg)
      1.2. "Path-Length" relative to Codebase Structure-Object Length 
            avg (Object CodeTime relative against codebase codetime total, code-object structure average) 
      1.3. "Criticality" or "incidince" is what is that space-time component to that codebase. 
      1.4. "Actuality" gives us a quantifiable measurment of the degree of a statement to its value for a given codebase. 

    Frome these basic definitions, its not perfect but, a general idea of a portion of code to its total implication to a cb should 
    be extractable/representable/measurable. 
    
    Goal-0: how much of Rust Code, and to what value to a codebase body is spent in Unsafe time. 
    Goal-1: [Deeper Sem Model required]: Could that code, portions, all of, or alternative actions have occured as safe. 
    Goal-N: Back to Sec-Analizer: What degree of cve producing code of Rust occurs as UNSAFE -> How unsafe is unsafe?? 

   Projects to look into/consider as supporting or inspiring:
      - GitHub/Semantic Haskell Project
      - GitHub/CodeQL


   Open Questions:
      - Can CodeQL Constructs support the question-space of this project?
      - Does Semantic have a full sem-model or in name only? 


Other/Related Projects:
   - rust-features (https://github.com/rustsystems/)
   - rust-codebases (https://github.com/rustsystems/)
   - rust-security-analysis (https://github.com/rustsystems/)
