package com.hourly.app.feature.payroll.data

import com.hourly.app.feature.payroll.domain.model.Payroll

/** Boundary for a future HTTP implementation; current implementation is deliberately local/offline. */
interface PayrollApi {
    suspend fun fetchPayrolls(): List<Payroll>
    suspend fun upsertPayroll(payroll: Payroll): Payroll
    suspend fun deletePayroll(id: String)
}
