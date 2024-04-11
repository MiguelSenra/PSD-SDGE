-module(main_server).
-export([start/1, stop/1]).
-import(login_manager, [start/0,create_account/2,close_account/2,login/2,logout/1,online/0]).

start(Port) -> spawn(fun() -> server(Port) end).
stop(Server) -> Server ! stop.

server(Port) ->
  {ok, LSock} = gen_tcp:listen(Port, [binary, {active, once}, {packet, 0},
                                      {reuseaddr, true}]),
  Login_Manager = spawn(fun()-> login_manager:start() end),
  spawn(fun() -> acceptor(LSock,Login_Manager) end),
  receive stop -> ok end.

acceptor(LSock,Login_Manager) ->
  {ok, Sock} = gen_tcp:accept(LSock),
  spawn(fun() -> acceptor(LSock,Login_Manager) end),
  %Users ! {enter, self()},
  process_tcp_messages(Sock,Login_Manager).

process_tcp_messages(Sock,Login_Manager) ->
    receive
        {tcp, _, Data} ->
            Data1 = binary_to_term(Data),
            case Data1 of 
                {register, Credentials} ->
                    io:format("user register~n", []),
                     Login_Manager:create_account(Credentials),
                    process_tcp_messages(Sock,Login_Manager);
                {login, _} ->
                    io:format("user login~n", []),
                     Login_Manager:login(Data),
                    process_tcp_messages(Sock,Login_Manager);
                Dados ->
                    io:format("nada ~n~p", [Dados]),
                    process_tcp_messages(Sock,Login_Manager)
            end
            
        %{tcp_closed, _} ->
        %  Login_Manager ! {leave, self()};
        %{tcp_error, _, _} ->
        %  Login_Manager ! {leave, self()}
    end.

