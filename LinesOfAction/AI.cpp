//#undef DEBUG
#include <bits/stdc++.h>
using namespace std;
using ll = long long;
const ll llinf = (1ll<<61)-1;
const char lf = '\n', splf[] = " \n";
#define sz(a) int(a.size())
#define all(x) begin(x), end(x)
#define TCC template<class C
#define OSO ostream &operator<<(ostream &os, const
#ifdef DEBUG
const int DEBUG_END = 26;
#define DOS cout
#include <debug.h>
#else
#define bug(args...) void()
#define cbug(a, args...)
#endif
#define ASSERT(a, o, b, args...) if (!((a)o(b))) bug(a, b, ##args), assert((a)o(b));
#define fi first
#define se second
#define r first
#define c second
#define Pair make_pair
const int inf = 1000000007, NONE = 2, dir = 8, dirr[] = {-1,-1,-1,0,0,1,1,1}, dirc[] = {-1,0,1,-1,1,-1,0,1};
int bs, tl;
auto initTime = chrono::steady_clock::now();

long elapsedMS(){
    return chrono::duration_cast<chrono::milliseconds>(chrono::steady_clock::now() - initTime).count();
}

bool timeUp() {
    return elapsedMS() >= tl;
}

using Cell = pair<int, int>;
using Move = pair<Cell, Cell>;

bool isValid(int row, int col) {
    return row >= 0 && row < bs && col >= 0 && col < bs;
}

vector<Cell> destinations(const vector<vector<int> > &grid, Cell cur) {
    int curr = cur.r, curc = cur.c;
    assert(grid[curr][curc] != NONE);
    vector<Cell> ret;
    for (int i = 0; i < dir; i++) {
        int cnt = 0, minOpponent = bs, nMov = 0, nr = curr, nc = curc;
        while (isValid(nr + dirr[i], nc + dirc[i])) {
            nr += dirr[i];
            nc += dirc[i];
            ++nMov;
            if (grid[nr][nc] != NONE) {
                ++cnt;
                if (grid[nr][nc] != grid[curr][curc]) minOpponent = min(minOpponent, nMov);
            }
        }
        if (minOpponent >= cnt) {
            nr = curr + cnt*dirr[i];
            nc = curc + cnt*dirc[i];
            if (grid[nr][nc] != grid[curr][curc]) ret.push_back({nr, nc});
        }
    }
    return ret;
}


struct Position {
    array<vector<Cell>, 2> pieces;  //pieces[0]->opponent's pieces, pieces[1]->my pieces

    vector<vector<int> > toGrid() {
        vector<vector<int> > grid(bs, vector<int>(bs, NONE));
        for (int i = 0; i < 2; ++i) {
            for (auto &j: pieces[i]) grid[j.r][j.c] = i;
        }
        return grid;
    }

    vector<Move> moves(bool player) {
        vector<Move> ret;
        for (auto &i: pieces[player]) {
            auto dests = destinations(toGrid(), i);
            for (auto &j: dests) ret.push_back({i, j});
        }
        random_shuffle(all(ret));
        return ret;
    }

    void move(bool player, Move mov) {
        pieces[player].erase(find(all(pieces[player]), mov.fi));
        bool opponent = !player;
        pieces[opponent].erase(remove(all(pieces[opponent]), mov.se), end(pieces[opponent]));
        pieces[player].push_back(mov.se);
    }

    int area(int player) {
        int minr = inf, minc = inf, maxr = -inf, maxc = -inf;
        for (auto &piece: pieces[player]) {
            maxr = max(maxr, piece.r);
            minr = min(minr, piece.r);
            maxc = max(maxc, piece.c);
            minc = min(minc, piece.c);
        }
        return pieces[player].empty() ? 0 : (maxr-minr+1)*(maxc-minc+1);
    }

    int connectedness(int player) {
        int ret = 0, cnt = 0;
        auto grid = toGrid();
        for (auto &piece: pieces[player]) {
            ++cnt;
            int i = piece.r, j = piece.c;
            if (grid[i][j] != player) continue;
            for (int d = 0; d < dir; ++d) {
                int ni = i+dirr[d], nj = j+dirc[d];
                ret += isValid(ni, nj) and grid[ni][nj] == player;
            }
        }
        return cnt ? ret*100/cnt : 10000;
    }

    int density(int player) {
        int sumr = 0, sumc = 0, sumd = 0, cnt = sz(pieces[player]);
        if (cnt <= 1) return 10000;
        for (auto &piece: pieces[player]) {
            sumr += piece.r;
            sumc += piece.c;
        }
        Cell com = Pair(int(round(sumr/double(cnt))), int(round(sumc/double(cnt))));
        for (auto &piece: pieces[player]) {
            int difr = abs(piece.r-com.r), difc = abs(piece.c-com.c);
            sumd += difr+difc-min(difr, difc);
        }
        int sumSurplusDistance = sumd-(cnt-1)-max(0, cnt-9);
        return -sumSurplusDistance*100/cnt;
    }

    int mobility(int player) {
        int ret = 0;
        auto grid = toGrid();
        auto allMoves = moves(player);
        for (auto &i: allMoves) {
            int val = 2;
            if (grid[i.se.r][i.se.c] == !player) val <<= 1;
            if (!isValid(i.se.r-1, i.se.c-1) or !isValid(i.se.r+1, i.se.c+1)) val >>= 1;
            ret += val;
        }
        return ret;
    }

    int scheme(int player) {
        return density(player);
    }

    int staticEval() {
        int ret = scheme(1)-scheme(0);

        assert(ret > -inf and ret < inf);
        return ret;
    }
};


Position initPosition;


int minimax(Position position, int depth, int alpha, int beta, bool player) {
    assert(depth >= 0);
    auto moves = position.moves(player);
    if (depth == 0 or moves.empty() or timeUp()) return position.staticEval();

    int maxEval = -inf, minEval = inf;
    for (auto &i: moves) {
        auto child = position;
        child.move(player, i);
        int eval = minimax(child, depth-1, alpha, beta, !player);
        maxEval = max(maxEval, eval);
        minEval = min(minEval, eval);
        if (player) alpha = max(alpha, eval);
        else beta = min(beta, eval);
        if (alpha >= beta or timeUp()) break;
    }
    return player ? maxEval : minEval;
}

Move iterativeDeepening() {
    Move ret;
    auto moves = initPosition.moves(1);
    for (int d = 0;; ++d) {
        Move best = {};
        int mx = -inf;
        for (auto &i: moves) {
            auto child = initPosition;
            child.move(1, i);
            int eval = minimax(child, d, -inf, inf, 0);
            if (eval > mx) {
                mx = eval;
                best = i;
            }
        }
        if (timeUp()) {
            //bug(d);
            break;
        }
        ret = best;
    }
    return ret;
}

void outputMove(Move move) {
    ofstream out("shared_file.txt");
    out << move.fi.r << ' ' << move.fi.c << ' ' << move.se.r << ' ' << move.se.c << lf;
    out.close();
    exit(0);
}

int main() {
    unsigned seed = unsigned(initTime.time_since_epoch().count());
    //bug(seed);
    srand(seed);
    ifstream in("shared_file.txt");
    int turn;
    in >> turn >> bs;
    tl = bs == 6 ? 1000 : 2000;
    for (int i = 0; i < bs; ++i) {
        for (int j = 0, x; j < bs; ++j) {
            in >> x;
            if (x != NONE) initPosition.pieces[x == turn].push_back({i, j});
        }
    }
    in.close();
    auto move = iterativeDeepening();
    // bug(turn, move);
    outputMove(move);
}
