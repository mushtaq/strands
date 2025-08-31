package strands.rpc.examples.actor

def BankAccount(): BankAccountApi =
  var balance = 0
  
  (
    getBalance = () => balance,
    deposit = (amount: Int) => balance += amount
  )
