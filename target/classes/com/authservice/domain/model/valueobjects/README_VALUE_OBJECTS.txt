===========================================================
DOMAIN LAYER: VALUE OBJECTS (VO)
===========================================================

PURPOSE:
Value Objects represent descriptive aspects of the domain 
that have no identity but carry significant meaning and 
validation rules.

THE ARCHITECTURE:

1. IMMUTABILITY
   All Value Objects are immutable. Once created, their state 
   cannot change. We use 'final' fields for all data.

2. SELF-VALIDATION
   A Value Object is responsible for its own integrity.
   The constructor MUST validate the input (e.g., Email regex, 
   UUID format, IP range). If the input is invalid, throw 
   an IllegalArgumentException immediately.

3. EQUALITY BY STATE
   Equality is based on value, not memory reference. Always 
   override equals() and hashCode() to compare the inner 
   primitive values.

4. STATIC FACTORIES FOR DEPENDENCIES
   If a VO requires a Domain Port to be built (like Hashing 
   or User-Agent parsing), use a static .create() method 
   that accepts the Port interface as an argument.

SECURITY NOTE:
By using VOs, we eliminate "Primitive Obsession." We don't pass 
around raw Strings; we pass around validated Domain types, 
making the system immune to malformed data at the core level.