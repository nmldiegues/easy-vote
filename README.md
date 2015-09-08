Easy Vote app
============

*A prototype for web-based secure voting*

Performing online elections is almost for sure something modern society will eventually adopt.

For now, it is still necessary to educate people about this possibility, and prototype ideas to implement it.

EasyVote is a web-based application that provides one possible implementation, while trying to ensure integrity of the votes, their confidentiality, uniqueness of the votes, anonymity of the voters, audibility of an election, and non coercion for the voters. The key technique that it uses is called Blinding Mechanisms, introduced by David Chaum, and referenced in the (highly praised) Applied Criptography by Bruce Schneier. The simple idea of this technique is to allow a Trusted Authority to sign something (for instance, a vote) without becoming aware of its contents.

EasyVote is split in:
 * A GWT web front-end
 * A Java backend
 * A distributed component for Registration of voters
 * A centralized Trusted Authority
 * BallotBox back-ends
 * Persistence layers across all these components
