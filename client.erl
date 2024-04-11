-module(client).
-export([start/2]).

start(Host, Port) ->
    {ok, Socket} = gen_tcp:connect(Host, Port, []),
    loop(Socket).

loop(Socket) ->
    io:format("Enter command (register, login): "),
    Command = read_line(),
    %io:format(Command),
    case Command of
        {ok,["register"]} ->
            Username = read_line("Enter username: "),
            Password = read_line("Enter password: "),
            gen_tcp:send(Socket, term_to_binary({register, {Username, Password}})),
            loop(Socket);
        {ok,["login"]} ->
            %{ok,Username} = read_line("Enter username: "),
            %{ok,Password} = read_line("Enter password: "),
            
            gen_tcp:send(Socket, term_to_binary({login,{"EU","TU"}})),
            loop(Socket);
        _ ->
            io:format("Invalid command. Try again.~n"),
            gen_tcp:close(Socket)
    end.

read_line(Prompt) ->
    io:format(Prompt),
    io:fread("","~s").

read_line() ->
    read_line("").
