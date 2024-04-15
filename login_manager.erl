-module(login_manager).
-export([start/0,create_account/1,close_account/1,login/1,logout/1,online/0,stop/0]).


start() -> %register(?MODULE,spawn (fun() ->loop(#{}) end)).
           register(login_manager1,spawn (fun() ->loop(#{}) end)).

rpc(Request) ->
    login_manager1 ! {Request,self()},
    receive {Res,login_manager1} -> Res 
    end.


create_account({Username,Passwd}) ->
    rpc({create_account,Username,Passwd}).


close_account({Username,Passwd}) ->
    rpc({close_account,Username,Passwd}).

login({Username,Passwd}) -> 
    rpc({login,Username,Passwd}).

logout(Username) -> 
    rpc({logout,Username}).

online() -> rpc({online}).

stop() -> rpc({stop}).
% processo "servidor" 

handle({create_account,Username,Passwd},Map) ->
    io:format("handle ~n", []), 
     case maps:find(Username,Map) of
            error -> 
                    Map1= maps:put(Username, {Passwd,true},Map),
                    {ok,Map1};
            _ -> 
                {user_exists,Map}
        end;

handle({close_account,Username,Passwd},Map) -> 
    case maps:find(Username,Map) of
        {ok,{Passwd,_}} -> 
            {ok,maps:remove(Username,Map)};
        _ -> 
            {invalid,Map}
    end;

handle({login,Username,Passwd},Map) ->
    case maps:find(Username,Map) of
        {ok,{Passwd,_}} -> 
            {ok,maps:update(Username,{Passwd,true},Map)};
        _ -> 
            {invalid,Map}
    end;

handle({logout,Username,Passwd},Map) ->
    {Passwd,_}=maps:find(Username,Map),
    {ok,maps:update(Username,{Passwd,false},Map)};


handle({online},Map)->
    Pred = fun({_, true}) -> true;
               (_) -> false end,

    Map1=maps:filter(Pred,Map),
    maps:keys(Map1).


loop(Map) -> 
    receive 
        {{stop},From } -> From ! {ok,login_manager1};
        {Request,From} ->
            {Msg,NextState} = handle(Request,Map),
            io:format("tenho resposta ~n", []), 
            From ! {Msg,login_manager1},
            io:format("enviei ~n", []), 
            loop(NextState) 
    end . 
