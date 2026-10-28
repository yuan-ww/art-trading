import { createStore } from 'vuex';
import axios from 'axios';

export default createStore({
  state: {
    user: {
      username: '未登录', 
      avatarUrl: '', 
      id: null,
      role: null,  
    },
    artwork: null,
    isLoggedIn: false, 
  },
  mutations: {
    setArtwork(state, artwork) {
      state.artwork = artwork;
      localStorage.setItem('artwork', JSON.stringify(artwork)); 
    },
    setUser(state, user) {
      state.user.username = user.username || '未登录';
      state.user.id = user.id || null; 
      state.user.avatarUrl = user.avatarUrl || ''; 
      state.user.role = user.role || null; 
      localStorage.setItem('user', JSON.stringify(state.user));
    },
    setLoginStatus(state, status) {
      state.isLoggedIn = status;
      localStorage.setItem('isLoggedIn', status ? 'true' : 'false');
    },
  },
  actions: {
    updateArtwork({ commit }, artwork) {
      commit('setArtwork', artwork);
    },
    loadArtworkFromStorage({ commit }) {
      const artwork = JSON.parse(localStorage.getItem('artwork'));
      if (artwork) {
        commit('setArtwork', artwork);
      }
    },
    login({ commit }, user) {
      localStorage.setItem('user', JSON.stringify(user));
      localStorage.setItem('isLoggedIn', 'true');
      commit('setUser', user);
      commit('setLoginStatus', true);
    },
    async logout({ commit }) {
      try {
        await axios.post('/api/user/logout');
      } catch (error) {
        // Clear local state even when the server session has already expired.
      }
      localStorage.removeItem('user');
      localStorage.removeItem('isLoggedIn');
      commit('setUser', { username: '未登录', avatarUrl: '', id: null,role:null });
      commit('setLoginStatus', false);
    },
    loadUserFromStorage({ commit }) {
      const user = JSON.parse(localStorage.getItem('user'));
      const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
      if (user && isLoggedIn) {
        commit('setUser', user);
        commit('setLoginStatus', true);
      } else {
        commit('setUser', { username: '未登录', avatarUrl: '', id: null, role: null });
      commit('setLoginStatus', false);
      }
    },
    updateAvatar({ commit, state }, avatarUrl) {
      commit('setUser', { ...state.user, avatarUrl });
      localStorage.setItem('user', JSON.stringify(state.user)); 
    },
  },
  getters: {
     getArtwork(state) {
       return state.artwork;
    },
    getUsername(state) {
      return state.user.username;
    },
    getAvatarUrl(state) {
      return state.user.avatarUrl;
    },
    getUserId(state) {
      return state.user.id;
    },
    getUserRole(state) {
      return state.user.role;
    },
    isLoggedIn(state) {
      return state.isLoggedIn;
    },
  }
});
