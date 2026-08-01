<template>
  <div class="flex h-screen bg-gray-100">
    <!-- Sidebar Left Navigation -->
    <aside 
      class="bg-slate-900 text-white flex flex-col transition-all duration-300 shadow-xl"
      :class="isOperator ? 'w-0 hidden' : 'w-72'"
    >
      <!-- Logo Header -->
      <div class="p-4 bg-slate-950 flex items-center justify-between border-b border-slate-800">
        <div class="flex items-center space-x-3">
          <div class="w-10 h-10 rounded-lg bg-blue-600 flex items-center justify-center font-bold text-xl text-white">
            SW
          </div>
          <div>
            <h1 class="font-bold text-lg text-white tracking-wide">SWICO MES</h1>
            <p class="text-xs text-slate-400">OEE Management System</p>
          </div>
        </div>
      </div>

      <!-- Current User Info -->
      <div class="px-4 py-3 bg-slate-800/50 border-b border-slate-800 flex items-center justify-between">
        <div class="text-xs">
          <span class="text-slate-400 block">Quyền hiện tại / 權限:</span>
          <span class="font-semibold text-emerald-400">{{ currentRole }}</span>
        </div>
      </div>

      <!-- Navigation Menu -->
      <nav class="flex-1 overflow-y-auto p-3 space-y-1">
        <div v-for="menu in menuList" :key="menu.id" class="mb-2">
          <!-- Parent Menu Item -->
          <div 
            @click="toggleSubMenu(menu.id)"
            class="flex items-center justify-between px-3 py-3 rounded-lg cursor-pointer hover:bg-slate-800 transition text-slate-200 select-none"
          >
            <div class="flex items-center space-x-3">
              <span class="material-icons-outlined text-blue-400 text-xl">{{ menu.icon }}</span>
              <span class="font-medium text-sm">{{ menu.title }}</span>
            </div>
            <span 
              v-if="menu.children" 
              class="material-icons-outlined text-slate-400 transition-transform duration-200 text-sm"
              :class="openMenus.includes(menu.id) ? 'rotate-180' : ''"
            >
              expand_more
            </span>
          </div>

          <!-- Submenu Children -->
          <div 
            v-if="menu.children && openMenus.includes(menu.id)" 
            class="ml-4 pl-3 border-l-2 border-slate-700 mt-1 space-y-1"
          >
            <router-link
              v-for="sub in menu.children"
              :key="sub.id"
              :to="sub.path"
              class="flex items-center space-x-2 px-3 py-2.5 rounded-md text-xs text-slate-300 hover:text-white hover:bg-blue-600/30 transition"
              active-class="bg-blue-600 text-white font-semibold"
            >
              <span class="material-icons-outlined text-sm">{{ sub.icon }}</span>
              <span>{{ sub.title }}</span>
            </router-link>
          </div>
        </div>
      </nav>

      <!-- Footer / Quick Action -->
      <div class="p-3 border-t border-slate-800 bg-slate-950 text-center">
        <button 
          @click="logout"
          class="w-full py-2 bg-rose-600/20 hover:bg-rose-600 text-rose-300 hover:text-white text-xs font-semibold rounded-md transition duration-200"
        >
          Đăng xuất / 退出登錄
        </button>
      </div>
    </aside>

    <!-- Main Content Body -->
    <main class="flex-1 flex flex-col overflow-hidden">
      <!-- Top App Bar for Operator or Quick Info -->
      <header class="bg-white border-b border-gray-200 h-14 flex items-center justify-between px-6 shadow-sm">
        <div class="flex items-center space-x-2">
          <span class="text-gray-500 font-medium text-sm">Hệ Thống Quản Lý Báo Cáo OEE / OEE 生產報表系統</span>
        </div>
        <div class="flex items-center space-x-4">
          <span class="text-xs bg-blue-50 text-blue-700 px-3 py-1 rounded-full font-medium border border-blue-200">
            Hôm nay: {{ currentDate }}
          </span>
        </div>
      </header>

      <!-- View Router Render -->
      <div class="flex-1 overflow-auto p-6 bg-gray-50">
        <router-view></router-view>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

// Demo vai trò (Trong thực tế sẽ lấy từ Pinia Store hoặc LocalStorage sau khi Login)
const currentRole = ref('ROLE_MANAGER'); 

const menuList = ref([]);
const openMenus = ref(['1', '2']); // Mặc định mở menu 1 và 2

const isOperator = computed(() => currentRole.value === 'ROLE_OPERATOR');

const currentDate = new Date().toISOString().split('T')[0];

// Gọi API lấy Menu từ Spring Boot
const fetchMenu = async () => {
  try {
    const res = await fetch(`http://localhost:8080/api/menu?role=${currentRole.value}`);
    const data = await res.json();
    menuList.value = data;

    // Nếu là Công nhân (OPERATOR), tự động chuyển hướng thẳng tới màn hình nhập liệu
    if (isOperator.value) {
      router.push('/production/entry');
    }
  } catch (err) {
    console.error('Lỗi khi tải Menu:', err);
  }
};

const toggleSubMenu = (menuId) => {
  if (openMenus.value.includes(menuId)) {
    openMenus.value = openMenus.value.filter(id => id !== menuId);
  } else {
    openMenus.value.push(menuId);
  }
};

const logout = () => {
  alert('Đã đăng xuất');
  router.push('/login');
};

onMounted(() => {
  fetchMenu();
});
</script>

<style scoped>
/* Import Material Icons nếu dự án chưa tích hợp */
@import url('https://fonts.googleapis.com/icon?family=Material+Icons+Outlined');
</style>