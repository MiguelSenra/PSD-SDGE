-module(client).
-export([start/1]).

start(Port) ->
    {ok, Socket} = gen_tcp:connect("0.0.0.0", Port, []),
    loop(Socket).

loop(Socket) ->
    io:format("Enter command (register, login): "),
    Command = read_line(),
    %io:format(Command),
    case Command of
        "register" ->
            Username = read_line("Enter username: "),
            Password = read_line("Enter password: "),
            gen_tcp:send(Socket, term_to_binary({register, {Username, Password}})),
            loop(Socket);
        "login" ->
            Username = read_line("Enter username: "),
            Password = read_line("Enter password: "),
            
            gen_tcp:send(Socket, term_to_binary({login,{Username,Password}})),
            loop(Socket);
        _ ->
            io:format("Invalid command. Try again.~n"),
            gen_tcp:close(Socket)
    end.

read_line(Prompt) ->
    io:format(Prompt),
    {ok,[Value]}=io:fread("","~s"),
    Value.

read_line() ->
    read_line("").
