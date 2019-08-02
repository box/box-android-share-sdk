package com.box.androidsdk.share.vm

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.box.androidsdk.content.models.BoxCollaboration
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

    private lateinit var selectRoleShareVM: SelectRoleShareVM

    private val mockRolesList: List<BoxCollaboration.Role> = mock()
    private val mockAllowOwnerRole = true
    private val mockRole = BoxCollaboration.Role.EDITOR
    private val mockAllowRemove = true
    private val mockCollaboration: BoxCollaboration = mock()

    @Before
    fun setup() {
        selectRoleShareVM = SelectRoleShareVM()
    }

    @Test
    fun `test set selectedRole update the live data correctly`() {
        assertNull(selectRoleShareVM.selectedRole.value)
        selectRoleShareVM.setSelectedRole(mockRole)
        assertEquals(mockRole, selectRoleShareVM.selectedRole.value)
    }

    @Test
    fun `test set allow owner role update the variable correctly`() {
        assertEquals(false, selectRoleShareVM.isOwnerRoleAllowed)
        selectRoleShareVM.setAllowOwnerRole(mockAllowOwnerRole)

        assertEquals(mockAllowOwnerRole, selectRoleShareVM.isOwnerRoleAllowed)
    }

    @Test
    fun `test set allow remove update the variable correctly`() {
        assertEquals(false, selectRoleShareVM.isRemoveAllowed)
        selectRoleShareVM.setAllowRemove(mockAllowRemove)

        assertEquals(mockAllowRemove, selectRoleShareVM.isRemoveAllowed)
    }

    @Test
    fun `test set roles update the variable correctly`() {
        assertEquals(0, selectRoleShareVM.roles.size)
        selectRoleShareVM.roles = mockRolesList

        assertEquals(mockRolesList, selectRoleShareVM.roles)
    }

    @Test
    fun `test set collaboration update the variable correctly`() {
        assertNull(selectRoleShareVM.collaboration)
        selectRoleShareVM.collaboration = mockCollaboration

        assertEquals(mockCollaboration, selectRoleShareVM.collaboration)
    }


    @Test
    fun `test set send invitation enabled update the variable correctly`() {
        assertEquals(false, selectRoleShareVM.isSendInvitationEnabled.value)
        selectRoleShareVM.setSendInvitationEnabled(true)

        assertEquals(true, selectRoleShareVM.isSendInvitationEnabled.value)
    }

    @Test
    fun `test set show send update the variable correctly`() {
        assertEquals(true, selectRoleShareVM.isShowSend.value)
        selectRoleShareVM.setShowSend(false)

        assertEquals(false, selectRoleShareVM.isShowSend.value)
    }

    @Test
    fun `test set remove selected update the variable correctly`() {
        assertEquals(false, selectRoleShareVM.isRemoveSelected)
        selectRoleShareVM.isRemoveSelected = true

        assertEquals(true, selectRoleShareVM.isRemoveSelected)
    }

    @Test
    fun `test set name update the variable correctly`() {
        assertEquals("", selectRoleShareVM.name)
        selectRoleShareVM.name  = "user"

        assertEquals("user", selectRoleShareVM.name)
    }

}