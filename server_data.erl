-module(server_data).
-export([start_serverData/0,new_server/1,get_servers/0,stop_serverData/0]).


start_serverData() -> register(?MODULE,spawn (fun() ->loop([]) end)).

rpc(Request) ->
    ?MODULE ! {Request,self()},
    receive {Res,?MODULE} -> Res 
    end.


new_server(Lista_chaves) ->
    rpc({new_server,Lista_chaves}).

get_servers() ->
    rpc({get_servers}).

stop_serverData() -> rpc({stop}).
% processo "servidor" 

handle({new_server,Lista_chaves},Keys) ->
    io:format("handle ~n", []),
    case lists:member(Lista_chaves,Keys) of
        false -> 
                Keys1=lists:append(Keys, Lista_chaves),
                {lists:sort(fun({_, _, A}, {_, _, B}) -> A =< B end, Keys1),Keys1};
        _ -> 
            {user_exists,Keys}
    end;
    
handle({get_servers},Keys) ->
    {Keys,Keys}.

loop(Keys) -> 
    receive 
        {{stop},From } -> From ! {ok,?MODULE};
        {Request,From} ->
            {Msg,NextState} = handle(Request,Keys),
            From ! {{Msg},?MODULE},
            loop(NextState) 
    end .
