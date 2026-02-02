import com.hien.le.expenseoverview.db.ExpenseDao
import com.hien.le.expenseoverview.db.ExpenseItemEntity

class GetEntryWithExpensesUseCase(
    private val dao: ExpenseDao
) {
    suspend fun execute(dateIso: String): EntryLoadedData? {
        val entry = dao.getEntryByDate(dateIso) ?: return null
        val expenses = dao.listExpenseItemsByDate(dateIso)

        return EntryLoadedData(
            dateIso = entry.dateIso,
            bargeldCents = entry.bargeldCents,
            karteCents = entry.karteCents,
            note = entry.note,
            expenses = expenses
        )
    }
}

data class EntryLoadedData(
    val dateIso: String,
    val bargeldCents: Long,
    val karteCents: Long,
    val note: String?,
    val expenses: List<ExpenseItemEntity>
)