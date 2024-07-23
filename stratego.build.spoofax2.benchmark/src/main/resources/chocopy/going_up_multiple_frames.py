a : int = 10
def foo(x : int) -> int:
	b : int = 0

	def aux(i : int) -> int:
	    return b + i

	def bar(y : int) -> int:
		c : int = 0
		def baz(z : int) -> int:
			d : int = 0
			d = aux(c + 1)
			return a + x + y + z

		return baz(a + b + x)

	b = aux(x)
	return bar(b + 10)
print(foo(a))