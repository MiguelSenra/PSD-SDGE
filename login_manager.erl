-module(login_manager).
-export([start/0,create_account/2,close_account/2,login/2,logout/1,online/0]).


start() -> %register(?MODULE,spawn (fun() ->loop(#{}) end)).
           register(login_manager,spawn (fun() ->loop(#{}) end)).

rpc(Request) ->
    ?MODULE ! {Request,self()},
    receive {Res,?MODULE} -> Res end.

create_account(Username,Passwd) ->
    rpc({create_account,Username,Passwd}).

close_account(Username,Passwd) ->
    rpc({close_account,Username,Passwd}).

login(Username,Passwd) -> 
    rpc({login,Username,Passwd}).

logout(Username) -> 
    rpc({logout,Username}).

online() -> rpc({online}).

% processo "servidor" 

handle({create_account,Username,Passwd},Map) -> 
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
        {ok,{Passwd,false}} -> 
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
        {Request,From} ->
            {Msg,NextState} = handle(Request,Map),
            From ! {Msg,?MODULE},
            loop(NextState) 
    end . 
