package strands.rpc.examples.actor

type BankAccountApi = (
    getBalance: () => Int,
    deposit: (amount: Int) => Unit
)

def BankAccount(): BankAccountApi =
  var balance = 0

  (
    getBalance = () =>
      println("*********** Balance requested: " + balance)
      balance
    ,
    deposit = (amount: Int) => balance += amount
  )
