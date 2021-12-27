AI_game_LinesOfAction

Very similar to my previous project https://github.com/ShafinKhadem/Checkers-multiplayer,
but this time fixed previous mistakes (used MVC, decoupled UI and game state etc.) and
added AI for single player instead of LAN multiplayer.

Rules of the game:
http://www.boardspace.net/loa/english/index.html#howto-play (also check the unusual endgames)
https://en.wikipedia.org/wiki/Lines_of_Action 
(We are considering no draw)
http://www.iggamecenter.com/info/en/loa.html (consider red checkers as black here)

AI is implemented using C++ program implementing Minimax algorithm with alpha beta pruning (with various heuristics)
