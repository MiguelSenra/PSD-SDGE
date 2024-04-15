-module(main_server).
-export([start/1, stop/1]).
-import(login_manager, [start/0,create_account/1,close_account/1,login/1,logout/1,online/0,stop/0]).
-import (albuns, [start_server_albuns/0,create_Album/1,stop_server_albuns/0]).

start(Port) -> spawn(fun() -> server(Port) end).
stop(Server) -> Server ! stop.

%{active,once } é para limitar os sockets a uma mensagem. Para gerir ataques DDoS por exemplo
server(Port) ->
    {ok, LSock} = gen_tcp:listen(Port, [binary, {active, true}, {packet, 0},
                                      {reuseaddr, true}]),
    spawn(fun()-> login_manager:start() end),
    spawn(fun()-> albuns:start_server_albuns() end),
    spawn(fun() -> acceptor(LSock) end),
    receive stop -> 
        login_manager:stop(),
        albuns:stop_server_albuns(),
        ok 
    end.

acceptor(LSock) ->
    {ok, Sock} = gen_tcp:accept(LSock),
    spawn(fun() -> acceptor(LSock) end),
    %Users ! {enter, self()},
    process_tcp_messages(Sock).

process_tcp_messages(Sock) ->
    io:format("a processar~n", []),
    receive
        {tcp, _, Data} ->
            Data1 = binary_to_term(Data),
            case Data1 of 
                {register, Credentials} ->
                    io:format("user register~n~p", [Credentials]),
                    Res=login_manager:create_account(Credentials),
                    io:format("Estado do registo:~n~p", [Res]),
                    %io:format("resposta~n~p", [Res]),
                    process_tcp_messages(Sock);
                {login, Credentials} ->
                    io:format("user login~n", []),
                    Res=login_manager:login(Credentials),
                    io:format("Estado do login:~n~p", [Res]),
                    process_tcp_messages(Sock);
                {create_Album,Values} ->
                    Res=albuns:create_Album(Values),
                    io:format("Estado do album:~n~p", [Res]),
                    process_tcp_messages(Sock);

                Dados ->
                    io:format("nada ~n~p", [Dados]),
                    process_tcp_messages(Sock)
            end;
            
        {tcp_closed, _} -> ok;
        %  Login_Manager ! {leave, self()};
        {tcp_error, _, _} -> ok;
        %  Login_Manager ! {leave, self()}
        Outro->  io:format("outros dados~n~p", [Outro])
    end.

