Rules of the game:
http://www.boardspace.net/loa/english/index.html#howto-play (also check the unusual endgames)
https://en.wikipedia.org/wiki/Lines_of_Action 
(We are considering no draw)
http://www.iggamecenter.com/info/en/loa.html (consider red checkers as black here)

Gameplay video:

https://user-images.githubusercontent.com/26321479/148691778-f1460a0f-b63d-4f88-95c3-fb9f25731d98.mp4


Very similar to my previous project https://github.com/ShafinKhadem/Checkers-LAN-multiplayer,
but this time fixed previous mistakes (used MVC, decoupled UI and game state etc.) and
added AI for single player instead of LAN multiplayer.

AI is implemented using C++ implementing Minimax algorithm with alpha beta pruning (with various heuristics).
AI goes as deep as possible in 2 seconds.

gradle create jar: `./gradlew jar`

Compile `AI.cpp` and name the executable as `AI`. then run `java -jar LinesOfAction.jar` from the same directory as `AI`.
