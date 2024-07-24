def f(x:int) -> int:
    print("start f")
    print(x)
    if x > 0:
        g(x, 1)
    print("end f")
    return x


def g(y:int, z:int) -> object:
    print("start g")
    print(y)
    print(z)
    f(y - z)
    print("end g")

def h(msg: str) -> object:
    print(msg)

print(f(4))