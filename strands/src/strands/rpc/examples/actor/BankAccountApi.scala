package strands.rpc.examples.actor

type BankAccountApi = (
    getBalance: () => Int,
    deposit: (amount: Int) => Unit
)
