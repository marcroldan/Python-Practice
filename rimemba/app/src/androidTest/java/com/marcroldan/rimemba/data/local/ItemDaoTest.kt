package com.marcroldan.rimemba.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.marcroldan.rimemba.data.local.dao.ItemDao
import com.marcroldan.rimemba.data.local.entity.ItemEntity
import com.marcroldan.rimemba.domain.model.TipoItem
import com.marcroldan.rimemba.domain.model.TipoRecurrencia
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class ItemDaoTest {

    private lateinit var database: RimembaDatabase
    private lateinit var dao: ItemDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, RimembaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.itemDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun nota(texto: String, creadoEn: LocalDateTime) = ItemEntity(
        texto = texto,
        tipo = TipoItem.NOTA,
        fechaHora = null,
        creadoEn = creadoEn
    )

    private fun recordatorio(texto: String, fechaHora: LocalDateTime, creadoEn: LocalDateTime) = ItemEntity(
        texto = texto,
        tipo = TipoItem.RECORDATORIO,
        fechaHora = fechaHora,
        creadoEn = creadoEn
    )

    @Test
    fun insertarYLeerDevuelveElMismoItem() = runTest {
        val ahora = LocalDateTime.of(2026, 7, 16, 10, 0)
        val id = dao.insert(nota("Comprar leche", ahora))

        val leido = dao.getById(id)

        assertEquals("Comprar leche", leido?.texto)
        assertEquals(TipoItem.NOTA, leido?.tipo)
        assertNull(leido?.fechaHora)
    }

    @Test
    fun actualizarYBorrarFuncionanSobreElMismoId() = runTest {
        val ahora = LocalDateTime.of(2026, 7, 16, 10, 0)
        val id = dao.insert(nota("Comprar leche", ahora))
        val guardado = dao.getById(id)!!

        dao.update(guardado.copy(texto = "Comprar leche y pan"))
        assertEquals("Comprar leche y pan", dao.getById(id)?.texto)

        dao.delete(dao.getById(id)!!)
        assertNull(dao.getById(id))
    }

    @Test
    fun observeAllOrdenaPendientesConFechaAntesQueNotas() = runTest {
        val ahora = LocalDateTime.of(2026, 7, 16, 10, 0)
        dao.insert(nota("Una nota", ahora))
        dao.insert(recordatorio("Llamar al médico", ahora.plusHours(2), ahora))

        val lista = dao.observeAll().first()

        assertEquals(TipoItem.RECORDATORIO, lista.first().tipo)
    }

    @Test
    fun observeTodayFiltraPorRangoDeFecha() = runTest {
        val hoy = LocalDateTime.of(2026, 7, 16, 10, 0)
        val manana = hoy.plusDays(1)
        dao.insert(recordatorio("Hoy", hoy, hoy))
        dao.insert(recordatorio("Mañana", manana, hoy))

        val inicio = hoy.toLocalDate().atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val fin = hoy.toLocalDate().atTime(23, 59, 59).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        val lista = dao.observeToday(inicio, fin).first()

        assertEquals(1, lista.size)
        assertEquals("Hoy", lista.first().texto)
    }

    @Test
    fun setCompletadoActualizaSoloEseCampo() = runTest {
        val ahora = LocalDateTime.of(2026, 7, 16, 10, 0)
        val id = dao.insert(nota("Comprar leche", ahora))

        dao.setCompletado(id, true)

        assertTrue(dao.getById(id)!!.completado)
    }

    @Test
    fun getPendingRemindersSoloDevuelveRecordatoriosNoCompletadosConFecha() = runTest {
        val ahora = LocalDateTime.of(2026, 7, 16, 10, 0)
        dao.insert(nota("Nota suelta", ahora))
        val idRecordatorio = dao.insert(recordatorio("Pendiente", ahora.plusHours(1), ahora))
        val idCompletado = dao.insert(recordatorio("Ya hecho", ahora.plusHours(2), ahora))
        dao.setCompletado(idCompletado, true)

        val pendientes = dao.getPendingReminders()

        assertEquals(1, pendientes.size)
        assertEquals(idRecordatorio, pendientes.first().id)
    }
}
