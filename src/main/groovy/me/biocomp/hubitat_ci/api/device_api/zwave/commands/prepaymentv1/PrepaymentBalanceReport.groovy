package me.biocomp.hubitat_ci.api.device_api.zwave.commands.prepaymentv1

trait PrepaymentBalanceReport {
    abstract java.lang.String format()
    abstract java.lang.Short getBalancePrecision()
    abstract java.lang.Short getBalanceType()
    abstract java.lang.Integer getBalanceValue()
    abstract java.lang.String getCMD()
    abstract java.lang.Short getCommandClassId()
    abstract java.lang.Short getCommandId()
    abstract java.lang.Integer getCurrency()
    abstract java.lang.Integer getDebt()
    abstract java.lang.Short getDebtPrecision()
    abstract java.lang.Short getDebtRecoveryPercentage()
    abstract java.lang.Integer getEmerCredit()
    abstract java.lang.Short getEmerCreditPrecision()
    abstract java.lang.Short getMeterType()
    abstract java.util.List getPayload()
    abstract java.lang.Short getScale()
    abstract void setBalancePrecision(java.lang.Short a)
    abstract void setBalanceType(java.lang.Short a)
    abstract void setBalanceValue(java.lang.Integer a)
    abstract void setCurrency(java.lang.Integer a)
    abstract void setDebt(java.lang.Integer a)
    abstract void setDebtPrecision(java.lang.Short a)
    abstract void setDebtRecoveryPercentage(java.lang.Short a)
    abstract void setEmerCredit(java.lang.Integer a)
    abstract void setEmerCreditPrecision(java.lang.Short a)
    abstract void setMeterType(java.lang.Short a)
    abstract void setScale(java.lang.Short a)
}