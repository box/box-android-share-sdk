package com.box.androidsdk.share.vm

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.box.androidsdk.content.models.BoxCollaboration
import com.box.androidsdk.content.models.BoxCollaborationItem
import com.box.androidsdk.share.sharerepo.BaseShareRepo
import com.nhaarman.mockitokotlin2.mock
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class SelectRoleVMTest {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    private val mockRepo: BaseShareRepo = mock()
    private val mockShareItem: BoxCollaborationItem = mock()
    private lateinit var selectRoleVM: SelectRoleVM

    private val mockRolesList: List<BoxCollaboration.Role> = mock()
    private val mockAllowOwnerRole = true
    private val mockRole = BoxCollaboration.Role.EDITOR
    private val mockAllowRemove = true
    private val mockCollaboration: BoxCollaboration = mock()

    @Before
    fun setup() {
        selectRoleVM = SelectRoleVM(mockRepo, mockShareItem)
    }

    @Test
    fun `test update roles update live data correctly`() {
        assertNull(selectRoleVM.roles.value)

        selectRoleVM.updateRoles(mockRolesList)

        assertEquals(mockRolesList, selectRoleVM.roles.value)
    }

    @Test
    fun `test update selected role update live data correctly`() {
        assertNull(selectRoleVM.selectedRole.value)

        selectRoleVM.updateSelectedRole(mockRole)

        assertEquals(mockRole, selectRoleVM.selectedRole.value)
    }

    @Test
    fun `test update allow owner role update live data correctly`() {
        assertNull(selectRoleVM.allowOwnerRole.value)

        selectRoleVM.updateAllowOwnerRole(mockAllowOwnerRole)

        assertEquals(mockAllowOwnerRole, selectRoleVM.allowOwnerRole.value)
    }

    @Test
    fun `test update allow remove update live data correctly`() {
        assertNull(selectRoleVM.allowRemove.value)

        selectRoleVM.updateAllowRemove(mockAllowRemove)

        assertEquals(mockAllowRemove, selectRoleVM.allowRemove.value)
    }

    @Test
    fun `test update collaboration live data correctly`() {
        assertNull(selectRoleVM.collaboration.value)

        selectRoleVM.updateCollaboration(mockCollaboration)

        assertEquals(mockCollaboration, selectRoleVM.collaboration.value)
    }



}