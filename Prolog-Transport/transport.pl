%top level predicate
minimumTransportCost(A, B, Cost):-
    open('solution.txt',write,Stream), 
    steppingStone(A, B, Stream, Cost),
    close(Stream).

%sets up and calls the stepping stone algorithm, returns true if algorithm succesfully completes
steppingStone(File1, File2, Stream, Cost):-  
    open(File1,read,F1), readAll(F1,Matrix), close(F1), open(File2,read,F2), readAll(F2,Costs), close(F2),
    getCols(Matrix, C), length(Matrix, Q), R is ((Q+1)/(C+2))-2, notDegenerate(C, R, Q, Matrix), nl(Stream), write(Stream, 'Route Costs:'), nl(Stream), printMatrix(Costs, C, Stream), nl(Stream), nl(Stream), 
    write(Stream, 'Initial Solution (Cost: '), calculateCost(Matrix, Costs, C, Q, CostInitial),
    write(Stream, CostInitial), write(Stream, ')'), nl(Stream), printMatrix(Matrix, C, Stream), nl(Stream), 
    runAlgorithm(Matrix, Costs, C, Q, R, Final), calculateCost(Final, Costs, C, Q, Cost),
    nl(Stream), write(Stream, 'FINAL (Cost: '), write(Stream, Cost), write(Stream, ')'), nl(Stream), printMatrix(Final, C, Stream), nl(Stream), nl(Stream), !.
steppingStone(_, _, Stream, _):-
        write('Degenerate Case'),
        write(Stream, 'Degenerate Case'), fail.


%runs the stepping stone algorithm, returns true if algorithm succesfully completes
runAlgorithm(Matrix, Costs, C, Q, R, Final):-
    findAllRoutes(Matrix, Routes, C, Q), findEmptyCells(Routes, Cells), findAllPaths(Cells, C, R, Routes, Paths), findMinPath(Paths, Costs, _, MinCost),
    MinCost>=0, Final = Matrix.
runAlgorithm(Matrix, Costs, C, Q, R, Final ):-
    findAllRoutes(Matrix, Routes, C, Q), findEmptyCells(Routes, Cells), findAllPaths(Cells, C, R, Routes, Paths), 
    findMinPath(Paths, Costs, MinPath, MinCost),
    MinCost<0, findAmount(MinPath, Amount), applyChanges(Matrix, MinPath, Amount, Intermediate),
     runAlgorithm(Intermediate, Costs, C, Q, R, Final).

%returns true if this is not a degenerate case
notDegenerate(C, R, Q, Matrix):-
    findAllRoutes(Matrix, Routes, C, Q), findEmptyCells(Routes, Cells), length(Cells, E), 
    not(C*R-E =:= C+R-E).

%Gets the number of columns in the matrix, returns true if the number of columns is found
getCols([c(Index, 'SUPPLY')|_], C):- C is Index-1, !.
getCols([c(_, _)|L], C):- getCols(L, C).


%returns true if two routes are in the same row
sameRow(c(Index, _), c(Index2, _), C):- B is div(Index, C+2), D is div(Index2, C+2), B=D.

%returns true if two routes are in the same row
sameColumn(c(Index, _), c(Index2, _), C):-0 is mod(Index-Index2, C+2).

%Prints a 2d representation of a matrix, returns true if succesfully printed
printMatrix([], _, _).
printMatrix([c(Index, Item)|L], C, Stream):- X is C+1, X is mod(Index, C+2), write(Stream, Item), nl(Stream), printMatrix(L, C, Stream), !.
printMatrix([c(Index, Item)|L], C, Stream):- X is C+1, not(X is mod(Index, C+2)), write(Stream, Item), write(Stream, ' '), printMatrix(L, C, Stream), !.

%returns true if all empty cells are found
findEmptyCells([], []).
findEmptyCells([c(Index, Item)|L], [c(Index, Item)|Cells]):- Item = '-', !, findEmptyCells(L, Cells).
findEmptyCells([c(Index, Item)|L], [c(Index, Item)|Cells]):- Item = 0, !, findEmptyCells(L, Cells).
findEmptyCells([c(_, _)|L], Cells):-findEmptyCells(L, Cells).

%returns true if all routes are found
findAllRoutes([], [], _, _).
findAllRoutes([c(Index, Item)|L], [c(Index, Item)|Routes], C, Q):- Index>C+2, Index<Q-C-2, not(0 is mod(Index, C+2)),
    X is C+1, not(X is mod(Index, C+2)),  !, findAllRoutes(L, Routes, C, Q).
findAllRoutes([c(_, _)|L], Routes, C, Q):-findAllRoutes(L, Routes, C, Q).

%finds a next vertical route, returns true if one is found
findNextRoute(c(Index, Item), [c(Index2, Item2)|_], Current, vert, C, _, Route):- sameColumn(c(Index, Item), c(Index2, Item2), C),
    not(member(c(Index2, Item2), Current)), not(c(Index, Item)=c(Index2, Item2)), not(Item2 = '-'), not(Item2 = 0), Route = c(Index2, Item2).
findNextRoute(c(Index, Item), [c(_, _)|L], Current, vert, C, _, Route):- findNextRoute(c(Index, Item), L, Current, vert, C, _, Route).
%finds a next horizontal route, returns true if one is found 
findNextRoute(c(Index, Item), [c(Index2, Item2)|_], Current, hor,C, _, Route):- sameRow(c(Index, Item),c(Index2, Item2), C), 
    not(member(c(Index2, Item2), Current)), not(c(Index, Item)=c(Index2, Item2)), not(Item2 = '-'), not(Item2 = 0), Route = c(Index2, Item2).
findNextRoute(c(Index, Item), [c(_, _)|L], Current, hor, C, _, Route):- findNextRoute(c(Index, Item), L, Current, hor, C, _, Route).

%finds a path from a cell and returns true if one is found
findPath(c(Index, Item), C, R, Routes, Path):- findPath([c(Index, Item)], C, R, Routes, Path).
findPath(Path,C,_,_, NewPath):- pathComplete(Path, C), reverse(Path, NewPath), !.
findPath([c(Index, Item)|L], C, R, Routes, Path):- length(L, X), 1 is mod(X+1, 2),
    findNextRoute(c(Index, Item), Routes, [c(Index, Item)|L], hor, C, R, Route),
    findPath([Route|[c(Index, Item)|L]], C, R, Routes, Path).
findPath([c(Index, Item)|L], C, R, Routes, Path):- length(L, X), 0 is mod(X+1, 2), 
    findNextRoute(c(Index, Item), Routes, [c(Index, Item)|L], vert, C, R, Route),
    findPath([Route|[c(Index, Item)|L]], C, R, Routes, Path).

%returns true if the path is complete
pathComplete([c(Index, Item)|L], C):- length(L, X), X+1>=4, 0 is mod(X+1, 2), 
    pathComplete(c(Index, Item), L, C), !.
pathComplete(c(Index, _), [c(Index2, _)|[]], C):-
    sameColumn(c(Index, _), c(Index2, _), C), !.
pathComplete(c(Index, Item), [c(_, _)|L], C):-
    pathComplete(c(Index, Item), L, C).

%returns true if all paths are found
findAllPaths([c(Index, Item)|L], C, R, Routes, Paths):- findAllPaths([c(Index, Item)|L], C, R, Routes, [], Paths).
findAllPaths([], _, _, _, Paths, Paths).
findAllPaths([c(Index, Item)|L], C, R, Routes, Accumulator, Paths):- 
    bagof(TEMP, findPath(c(Index, Item), C, R, Routes, TEMP),Pathss),
    append(Pathss, Accumulator, NewList),
    findAllPaths(L, C, R, Routes, NewList, Paths).
findAllPaths([c(Index, Item)|L], C, R, Routes, Accumulator, Paths):- 
    not(findPath(c(Index, Item), C, R, Routes, _)),
    findAllPaths(L, C, R, Routes, Accumulator, Paths).

%finds the route cost, returns true if the cost is found
getRouteCost(c(Index, _), [c(Index, Price)|_], Cost):-  Cost is Price, !.
getRouteCost(c(Index, Item), [c(_, _)|L], Cost):- getRouteCost(c(Index, Item), L, Cost).

%finds the path cost, returns true if cost is found
getPathCost([c(Index, Item)|L], Costs, Final):- getPathCost([c(Index, Item)|L], Costs, 0, 0, Final).
getPathCost([], _, Final, _, Final).
getPathCost([c(Index, Item)|L], Costs, Total, Count, Final):- 1 is mod(Count, 2),
    getRouteCost(c(Index, Item), Costs, Price), Total1 is Total-Price,
    Count1 is Count+1, getPathCost(L, Costs, Total1, Count1, Final), !. 
getPathCost([c(Index, Item)|L], Costs, Total, Count, Final):- 0 is mod(Count, 2), 
    getRouteCost(c(Index, Item), Costs, Price), Total1 is Total+Price,
    Count1 is Count+1, getPathCost(L, Costs, Total1, Count1, Final), !.

%finds the minimal cost path, returns true if one is found
findMinPath([Path|L], Costs,  MinPath, MinCost):- getPathCost(Path, Costs, CurrentCost), findMinPath(L, Costs, Path, CurrentCost, MinPath, MinCost).
findMinPath([], _, MinPath, MinCost, MinPath, MinCost).
findMinPath([Path|L], Costs, _, CurrentCost, MinPath, MinCost):- getPathCost(Path, Costs, Cost1), Cost1<CurrentCost, 
    findMinPath(L, Costs, Path, Cost1, MinPath, MinCost),!.
findMinPath([_|L], Costs, CurrentPath, CurrentCost, MinPath, MinCost):-
    findMinPath(L, Costs, CurrentPath, CurrentCost, MinPath, MinCost).

%finds the amount to be transfered in the new path, returns true if the amount is found
findAmount([c(_, Item)|L], Amount):-findAmount(L, Item, 0, Amount).
findAmount([], Amount, _, Amount).
findAmount([c(_, Item)|L], Current, Count, Amount):- 0 is mod(Count, 2), Current=0, Count1 is Count+1, findAmount(L, Item, Count1, Amount), !.
findAmount([c(_, Item)|L], Current, Count, Amount):- 0 is mod(Count, 2), Item<Current, Count1 is Count+1, findAmount(L, Item, Count1, Amount), !.
findAmount([c(_, _)|L], Current, Count, Amount):- Count1 is Count+1, findAmount(L, Current, Count1, Amount).

%apply a change to a cell, returns true if change successfuly applied
applyChange(Matrix, c(Index, Item), Amount, NewMatrix):- applyChange(Matrix, c(Index, Item), Amount, [], NewMatrix).
applyChange([c(Index, Item)|L], c(Index, Item), Amount, Accumulator, NewMatrix):-reverse(Accumulator, Reversed),
    NewAmount is Item+Amount, append(Reversed, [c(Index, NewAmount)|L], NewMatrix),!.
applyChange([c(Index, Item)|L], c(Index2, Item2), Amount, Accumulator, NewMatrix):- applyChange(L, c(Index2, Item2), Amount, [c(Index, Item)|Accumulator], NewMatrix).

%apply all changes from an optimal path, returns true if changes successfuly applied
applyChanges(Final, [], _, Final).
applyChanges(Matrix, [c(Index, Item)|L], Amount, Final):- Amount1 is -1*Amount, 
    applyChange(Matrix, c(Index, Item), Amount, NewMatrix), applyChanges(NewMatrix, L, Amount1, Final).


%calculates the cost of transport, returns true if cost successfuly calculated
calculateCost(Matrix, Costs, C, Q, Cost):-calculateCost(Matrix, Costs, C, Q, 0, Cost).
calculateCost([], _, _, _, Cost, Cost).
calculateCost([c(Index, Item)|L], Costs, C, Q, Current, Cost):- Index>C+2, Index<Q-C-2, not(0 is mod(Index, C+2)),
    X is C+1, not(X is mod(Index, C+2)), getRouteCost(c(Index, Item), Costs, Price), Amount is (Price*Item)+Current, 
    calculateCost(L, Costs, C, Q,Amount, Cost), !.
calculateCost([c(_, _)|L], Costs, C, Q, Current, Cost):- calculateCost(L, Costs, C, Q,Current, Cost).



%------------IO Code-------------------------------

readAll( InStream, L ) :- readAll( InStream, L, continue, 0). %called by user

% Read text file into list of strings and numbers
readAll( _, [], end, _ ) :- !. %stop reading if end of file

readAll( InStream, Out, continue, Index ) :-
    readWordNumber( InStream, W, Check), !,
    %write(W), write(' '), % Just for sanity

    addOrDont(InStream, W, Out, Check, Index).

addOrDont(InStream, '', L, Check, Index):- !,
    NewIndex is Index,
    readAll( InStream, L, Check, NewIndex).

addOrDont(InStream, W, Out, Check, Index):-
    NewIndex is Index+1,
    readAll( InStream, L, Check, NewIndex),
    addToList( L, W, Index, Out ).

% add A to the front of L if not an empty string
addToList( L, '', _, L) :- !.
addToList( L, A, Index, [c(Index,A)|L] ).

% read a white-space separated text or number
readWordNumber(InStream,W, Check):-
         get_code(InStream,Char),
         checkCharAndReadRest(Char,Chars,InStream, Check),
	 codes2NumOrWord(W,Chars).

% Convert list of codes into a number if possible to string otherwise
codes2NumOrWord(N,Chars) :-
    atom_codes(W,Chars),
    W = '-', !, N = 0.

codes2NumOrWord(N,Chars) :-
    atom_codes(W,Chars),
    atom_number(W,N),!.


codes2NumOrWord(W,Chars) :-
    atom_codes(W,Chars).
   
% Source: Learn Prolog Now!   
checkCharAndReadRest(10,[],_, continue):- !.
   
checkCharAndReadRest(32,[],_, continue):-  !.

checkCharAndReadRest( 9, [], _, continue) :- !. 
   
checkCharAndReadRest(-1,[],_, end):-  !.
   
checkCharAndReadRest(end_of_file,[],_, continue):-  !.
   
checkCharAndReadRest(Char,[Char|Chars],InStream, Check):-
         get_code(InStream,NextChar),
         checkCharAndReadRest(NextChar,Chars,InStream, Check).
