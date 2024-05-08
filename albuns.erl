-module(albuns).
-export([start_server_albuns/0,create_Album/1,stop_server_albuns/0,list_Album/1,add_editor/1,terminate_edit_Album/1,get_Album/1]).
-record(album, {users = [], content = #{}}).
%-record(file, {hash, pontuation = #{}}).

start_server_albuns() ->
    register(?MODULE,spawn (fun() ->loop({#{},#{}}) end)).


create_Album({Name,User}) ->
    rpc({create_album,Name,User}).

list_Album({User})->
    rpc({list_album,User}).

get_Album({Nome,User})->
    rpc({get_album,Nome,User}).

add_editor({Name, User, IP, Port})->
    rpc({add_editor, Name, User, IP, Port}).

terminate_edit_Album({Name, User,Members})->
    rpc({terminate_edit_Album, Name, User, Members}).
%update_Album(Album) ->
    %update_Album(Album).

stop_server_albuns() -> rpc({stop}).
% processo "servidor" 

rpc(Request) ->
    ?MODULE ! {Request,self()},
    receive {Res,?MODULE} -> Res 
    end.


handle({create_album, Name, User}, {Albuns, User_index}) ->
    A = #album{users = [User]},
    case maps:find(Name, Albuns) of
        {ok, _Value} -> {album_exists, {Albuns, User_index}};
        error ->
            New_Albuns = maps:put(Name, {A,[]}, Albuns),
            case maps:find(User, User_index) of
                {ok, Value} -> 
                    New_User_index=maps:update(User, [Name | Value], User_index),
                    {album_created,{New_Albuns,New_User_index}};
                error -> 
                    New_User_index=maps:put(User, [Name], User_index),
                    {album_created,{New_Albuns,New_User_index}}
            end
    end;

handle({list_album,User}, {Albuns, User_index}) ->
    case maps:find(User, User_index) of
        {ok, Value} -> {Value, {Albuns, User_index}};
        error -> {[],{Albuns, User_index}}
    end;

handle({get_album, Name, User}, {Albuns, User_index}) ->
    %Res_error= "Não possui autorização para acessar o album!",
    Res_error= no_autorization,
    case maps:find(User, User_index) of
        {ok, Value} -> case lists:member(Name, Value) of 
            true -> case maps:find(Name, Albuns) of
                    {ok, {Album,_}} -> 
                        {Album,{Albuns, User_index}};
                    error -> {no_exists,{Albuns, User_index}}              
                end;
            false -> {Res_error,{Albuns, User_index}}
        end;
        error ->  {Res_error,{Albuns, User_index}}
    end;

handle({add_editor, Name, User, IP, Port}, {Albuns, User_index}) ->
    case maps:find(User, User_index) of
        {ok, Value} -> case lists:member(Name, Value) of 
            true -> case maps:find(Name, Albuns) of
                    {ok, {Album,Editors}} -> 
                        New_Editors = lists:append(Editors, [{IP,Port,User}]),
                        New_Albuns = maps:update(Name, {Album,New_Editors}, Albuns),
                        {New_Editors,{New_Albuns, User_index}};
                    error -> {no_exists,{Albuns, User_index}}              
                end;
            false -> {no_autorization,{Albuns, User_index}}
        end;
        error ->  {no_autorization,{Albuns, User_index}}
    end;

handle({terminate_edit_Album, Name, User, Members}, {Albuns, User_index}) ->
    case maps:find(Name, Albuns) of
        {ok, {Album, Editors}} ->
            {Users_const, Users_removidos} = lists:partition(fun(X) -> lists:member(X, Members) end, Album#album.users),
            Users_novos = lists:filter(fun(X) -> not lists:member(X, Users_const) end, Members),
            New_Album = Album#album{users = lists:append(Users_novos, Users_const)},
            New_Editors = remove_user_from_editors(User, Editors),
            New_Albuns = maps:update(Name, {New_Album, New_Editors}, Albuns),
            New_User_index = lists:foldl(fun(X, Acc) ->
                case maps:find(X, Acc) of
                    {ok, Value} ->
                        NewValue = lists:append(Value, [Name]),
                        maps:update(X, NewValue, Acc);
                    error ->
                        maps:put(X, [Name], Acc)
                end
            end, User_index, Users_novos),
            Updated_User_index = lists:foldl(fun(X, Acc) ->
                case maps:find(X, Acc) of
                    {ok, Value} ->
                        NewValue = lists:delete(Name, Value),
                        maps:update(X, NewValue, Acc);
                    error ->
                        pass
                end
            end, New_User_index, Users_removidos),
            {ok,{New_Albuns, Updated_User_index}};
        error -> {no_exists, {Albuns, User_index}}
    end.

remove_user_from_editors(_User, []) ->
    [];
remove_user_from_editors(User, [{_, _, UserName} | Tail]) when UserName == User ->
    remove_user_from_editors(User, Tail);
remove_user_from_editors(User, [Head | Tail]) ->
    [Head | remove_user_from_editors(User, Tail)].



loop({Albuns,User_index}) -> 
    receive 
        {{stop},From } -> From ! {ok,?MODULE};
        {Request,From} ->
            {Msg,NextState} = handle(Request,{Albuns,User_index}),
            %io:format("tenho resposta ~n", []), 
            From ! {{Msg},?MODULE},
            %io:format("enviei ~n", []), 
            loop(NextState)
    end. 


