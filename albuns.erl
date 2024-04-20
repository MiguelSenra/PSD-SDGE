-module(albuns).
-export([start_server_albuns/0,create_Album/1,stop_server_albuns/0,list_Album/1,get_Album/1]).
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
            New_Albuns = maps:put(Name, A, Albuns),
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
                    {ok, Album} -> {Album,{Albuns, User_index}};
                    error -> {no_exists,{Albuns, User_index}}              
                end;
            false -> {Res_error,{Albuns, User_index}}
        end;
        error ->  {Res_error,{Albuns, User_index}}
    end.

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
