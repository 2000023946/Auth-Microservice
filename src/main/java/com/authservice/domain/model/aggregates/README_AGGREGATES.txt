===========================================================
DOMAIN LAYER: AGGREGATE CAPSULE PATTERN
===========================================================

PURPOSE:
This folder contains the Aggregate Roots of the domain. Each 
sub-package (e.g., /user, /session) is a "Domain Capsule."

THE THREE GOLDEN RULES:

1. PACKAGE-PRIVATE CONSTRUCTORS
   Aggregate constructors MUST NOT be public. This prevents 
   instantiation of an aggregate from outside the capsule.
   An aggregate can only be born through its Factory.

2. THE FACTORY AS THE GATEKEEPER
   Every aggregate package must have a [Name]Factory. 
   - Public create(...): For fresh objects (Login, Signup).
   - Public reconstitute(...): For loading existing state 
     from Persistence (SQL/Redis).
   The Factory is the only place where raw primitives (String, int) 
   are mapped into Domain Value Objects.

3. INVARIANT PROTECTION
   Business logic (locking an account, revoking a token) happens 
   inside the Aggregate. The Aggregate ensures it is ALWAYS 
   in a valid state. If the data is in memory, it is valid.

DATA FLOW:
Infrastructure (JSON/SQL) -> Factory -> Value Objects -> Aggregate