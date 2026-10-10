/**
 * OmniRecommend AI - Frontend Application
 * Handles omni-search, hybrid recommendations, multi-store comparison,
 * direct movie downloads, shortest path route maps, food sentiment, and admin analytics.
 */

const API_BASE = '/api';

const state = {
  activeUserId: 'usr-alice',
  activeCategory: 'ALL',
  minConfidence: 0.60,
  sortOrder: 'matchScore',
  currentView: 'USER', // 'USER' or 'ADMIN'
  users: [],
  recommendations: [],
  charts: {
    categoryCtr: null,
    timeline: null
  },
  map: null,
  mapRouteLayer: null,
  mapMarkers: []
};

// DOM Elements
const el = {
  userSelect: document.getElementById('userSelect'),
  activeUserName: document.getElementById('activeUserName'),
  btnUserMode: document.getElementById('btnUserMode'),
  btnAdminMode: document.getElementById('btnAdminMode'),
  btnOpenPreferences: document.getElementById('btnOpenPreferences'),
  btnRefreshFeed: document.getElementById('btnRefreshFeed'),
  
  userDashboardView: document.getElementById('userDashboardView'),
  adminDashboardView: document.getElementById('adminDashboardView'),
  
  searchForm: document.getElementById('searchForm'),
  searchInput: document.getElementById('searchInput'),
  categoryContainer: document.getElementById('categoryFilterContainer'),
  recommendationsGrid: document.getElementById('recommendationsGrid'),
  loadingIndicator: document.getElementById('loadingIndicator'),
  emptyResults: document.getElementById('emptyResults'),
  
  // Refinement Sidebar Elements
  sortSelect: document.getElementById('sortSelect'),
  minScoreSlider: document.getElementById('minScoreSlider'),
  minScoreVal: document.getElementById('minScoreVal'),
  btnResetFilters: document.getElementById('btnResetFilters'),
  btnClearFiltersEmpty: document.getElementById('btnClearFiltersEmpty'),
  btnSidebarPreferences: document.getElementById('btnSidebarPreferences'),
  resultsCount: document.getElementById('resultsCount'),
  
  // Modals
  modalProduct: document.getElementById('modalProduct'),
  modalMovie: document.getElementById('modalMovie'),
  modalRoute: document.getElementById('modalRoute'),
  modalFood: document.getElementById('modalFood'),
  modalPreferences: document.getElementById('modalPreferences'),
  
  // Admin Form & Controls
  adminSettingsForm: document.getElementById('adminSettingsForm'),
  btnSaveAdminSettings: document.getElementById('btnSaveAdminSettings'),
  btnResetSettings: document.getElementById('btnResetSettings'),
  btnRefreshAnalytics: document.getElementById('btnRefreshAnalytics'),
  adminToast: document.getElementById('adminToast'),
  adminToastMessage: document.getElementById('adminToastMessage'),
  btnCloseAdminToast: document.getElementById('btnCloseAdminToast'),
  settingsLastUpdated: document.getElementById('settingsLastUpdated'),
  userProfilesTableBody: document.getElementById('userProfilesTableBody'),
  aiSuggestionsList: document.getElementById('aiSuggestionsList'),
  
  // Preferences Form
  userPreferencesForm: document.getElementById('userPreferencesForm'),
  prefPriceSelect: document.getElementById('prefPriceSelect'),
  prefDietarySelect: document.getElementById('prefDietarySelect'),
  prefLocationInput: document.getElementById('prefLocationInput'),
  
  // Route Recalculate Form
  routeRecalculateForm: document.getElementById('routeRecalculateForm'),
  routeInputOrigin: document.getElementById('routeInputOrigin'),
  routeInputDest: document.getElementById('routeInputDest')
};

// --- INITIALIZATION ---
document.addEventListener('DOMContentLoaded', () => {
  initEventListeners();
  loadUsers();
  loadRecommendations();
  loadAdminSettings();
  loadAnalytics();
});

function initEventListeners() {
  // Mode toggling
  el.btnUserMode.addEventListener('click', () => switchView('USER'));
  el.btnAdminMode.addEventListener('click', () => switchView('ADMIN'));

  // User switcher
  el.userSelect.addEventListener('change', (e) => {
    state.activeUserId = e.target.value;
    const selectedOption = e.target.options[e.target.selectedIndex];
    el.activeUserName.textContent = selectedOption.text.split(' ')[0];
    loadRecommendations();
    loadAnalytics();
  });

  // Search form
  el.searchForm.addEventListener('submit', (e) => {
    e.preventDefault();
    const query = el.searchInput.value.trim();
    loadRecommendations(query);
  });

  // Quick Chips
  document.querySelectorAll('.quick-chip').forEach(chip => {
    chip.addEventListener('click', () => {
      const q = chip.getAttribute('data-q');
      el.searchInput.value = q;
      loadRecommendations(q);
    });
  });

  // Category filter pills
  el.categoryContainer.querySelectorAll('.cat-pill').forEach(btn => {
    btn.addEventListener('click', () => {
      el.categoryContainer.querySelectorAll('.cat-pill').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      state.activeCategory = btn.getAttribute('data-category');
      loadRecommendations(el.searchInput.value.trim());
    });
  });

  // Refresh feed button
  el.btnRefreshFeed.addEventListener('click', () => {
    el.searchInput.value = '';
    loadRecommendations();
  });

  // Refinement sidebar controls
  if (el.sortSelect) {
    el.sortSelect.addEventListener('change', (e) => {
      state.sortOrder = e.target.value;
      renderCurrentFilteredRecommendations();
    });
  }

  if (el.minScoreSlider) {
    el.minScoreSlider.addEventListener('input', (e) => {
      state.minConfidence = parseFloat(e.target.value);
      if (el.minScoreVal) {
        el.minScoreVal.textContent = Math.round(state.minConfidence * 100) + '%';
      }
      renderCurrentFilteredRecommendations();
    });
  }

  if (el.btnResetFilters) {
    el.btnResetFilters.addEventListener('click', resetSidebarFilters);
  }
  if (el.btnClearFiltersEmpty) {
    el.btnClearFiltersEmpty.addEventListener('click', resetSidebarFilters);
  }
  if (el.btnSidebarPreferences) {
    el.btnSidebarPreferences.addEventListener('click', openPreferencesModal);
  }

  // Modal close handlers
  document.querySelectorAll('.modal-close-btn').forEach(btn => {
    btn.addEventListener('click', closeAllModals);
  });
  window.addEventListener('click', (e) => {
    if (e.target.classList.contains('fixed') && !e.target.classList.contains('hidden')) {
      closeAllModals();
    }
  });

  // Preferences modal
  el.btnOpenPreferences.addEventListener('click', openPreferencesModal);
  el.userPreferencesForm.addEventListener('submit', savePreferences);

  // Route recalculation
  el.routeRecalculateForm.addEventListener('submit', (e) => {
    e.preventDefault();
    const orig = el.routeInputOrigin.value.trim();
    const dest = el.routeInputDest.value.trim();
    openRouteModal(orig, dest);
  });

  // Admin sliders display binding
  bindAdminSlider('inputContentWeight', 'valContentWeight');
  bindAdminSlider('inputCollaborativeWeight', 'valCollaborativeWeight');
  bindAdminSlider('inputPopularityWeight', 'valPopularityWeight');
  bindAdminSlider('inputExplorationRate', 'valExplorationRate');
  bindAdminSlider('inputMinConfidenceThreshold', 'valMinConfidenceThreshold');

  // Admin Presets
  document.querySelectorAll('.preset-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const p = btn.getAttribute('data-preset');
      applyAlgorithmPreset(p);
    });
  });

  // Admin save & reset
  el.btnSaveAdminSettings.addEventListener('click', saveAdminSettings);
  el.btnResetSettings.addEventListener('click', resetAdminSettings);
  el.btnRefreshAnalytics.addEventListener('click', loadAnalytics);
  el.btnCloseAdminToast.addEventListener('click', () => el.adminToast.classList.add('hidden'));
}

function bindAdminSlider(inputId, valId) {
  const input = document.getElementById(inputId);
  const val = document.getElementById(valId);
  if (input && val) {
    input.addEventListener('input', () => {
      val.textContent = parseFloat(input.value).toFixed(2);
    });
  }
}

function switchView(view) {
  state.currentView = view;
  if (view === 'USER') {
    el.userDashboardView.classList.remove('hidden');
    el.adminDashboardView.classList.add('hidden');
    el.btnUserMode.classList.add('active');
    el.btnAdminMode.classList.remove('active');
    el.btnAdminMode.classList.add('text-slate-400');
  } else {
    el.userDashboardView.classList.add('hidden');
    el.adminDashboardView.classList.remove('hidden');
    el.btnAdminMode.classList.add('active');
    el.btnAdminMode.classList.remove('text-slate-400');
    el.btnUserMode.classList.remove('active');
    loadAnalytics();
  }
}

// --- DATA FETCHING & RECOMMENDATIONS ---
async function loadUsers() {
  try {
    const res = await fetch(`${API_BASE}/users`);
    const data = await res.json();
    if (data.users) {
      state.users = data.users;
      renderUserProfilesTable(data.users);
    }
  } catch (err) {
    console.error('Failed to load users:', err);
  }
}

function renderUserProfilesTable(users) {
  el.userProfilesTableBody.innerHTML = '';
  users.forEach(u => {
    const tr = document.createElement('tr');
    tr.className = 'hover:bg-slate-800/40 transition';
    const cats = (u.preferences && u.preferences.preferredCategories) ? u.preferences.preferredCategories.join(', ') : 'All';
    const loc = (u.preferences && u.preferences.currentLocation) ? u.preferences.currentLocation : 'Not set';
    tr.innerHTML = `
      <td class="px-4 py-3 font-mono text-indigo-400 font-medium">${escapeHtml(u.id)}</td>
      <td class="px-4 py-3 font-bold text-white">${escapeHtml(u.username)}</td>
      <td class="px-4 py-3 text-slate-400">${escapeHtml(u.email)}</td>
      <td class="px-4 py-3"><span class="px-2 py-0.5 rounded text-[10px] font-bold ${u.role === 'ADMIN' ? 'bg-amber-500/20 text-amber-400' : 'bg-indigo-500/20 text-indigo-400'}">${escapeHtml(u.role)}</span></td>
      <td class="px-4 py-3 text-slate-300">${escapeHtml(cats)}</td>
      <td class="px-4 py-3 text-slate-400">${escapeHtml(loc)}</td>
    `;
    el.userProfilesTableBody.appendChild(tr);
  });
}

async function loadRecommendations(query = '') {
  el.loadingIndicator.classList.remove('hidden');
  el.emptyResults.classList.add('hidden');
  el.recommendationsGrid.innerHTML = '';

  try {
    let url = `${API_BASE}/recommendations?userId=${state.activeUserId}&category=${state.activeCategory}`;
    if (query) {
      url = `${API_BASE}/search?q=${encodeURIComponent(query)}&userId=${state.activeUserId}&category=${state.activeCategory}`;
    }

    const res = await fetch(url);
    const data = await res.json();
    const items = data.recommendations || data.results || [];

    state.recommendations = items;
    el.loadingIndicator.classList.add('hidden');

    renderCurrentFilteredRecommendations();
  } catch (err) {
    el.loadingIndicator.classList.add('hidden');
    el.emptyResults.classList.remove('hidden');
    console.error('Failed to load recommendations:', err);
  }
}

function renderCurrentFilteredRecommendations() {
  let items = [...(state.recommendations || [])];

  // Category filter
  if (state.activeCategory && state.activeCategory !== 'ALL') {
    items = items.filter(i => (i.category || '').toUpperCase() === state.activeCategory.toUpperCase());
  }

  // Min confidence threshold filter
  items = items.filter(i => (i.matchScore || 0.85) >= state.minConfidence);

  // Sorting
  if (state.sortOrder === 'matchScore') {
    items.sort((a, b) => (b.matchScore || 0) - (a.matchScore || 0));
  } else if (state.sortOrder === 'popularity') {
    items.sort((a, b) => ((b.rating || 0) * (b.reviewCount || 1)) - ((a.rating || 0) * (a.reviewCount || 1)));
  } else if (state.sortOrder === 'priceAsc') {
    items.sort((a, b) => (a.price != null ? a.price : 999999) - (b.price != null ? b.price : 999999));
  } else if (state.sortOrder === 'priceDesc') {
    items.sort((a, b) => (b.price != null ? b.price : 0) - (a.price != null ? a.price : 0));
  }

  if (el.resultsCount) {
    el.resultsCount.textContent = items.length;
  }

  if (items.length === 0) {
    el.emptyResults.classList.remove('hidden');
    el.recommendationsGrid.innerHTML = '';
  } else {
    el.emptyResults.classList.add('hidden');
    renderRecommendationsGrid(items);
  }
}

function resetSidebarFilters() {
  state.activeCategory = 'ALL';
  state.minConfidence = 0.60;
  state.sortOrder = 'matchScore';

  if (el.sortSelect) el.sortSelect.value = 'matchScore';
  if (el.minScoreSlider) el.minScoreSlider.value = 0.60;
  if (el.minScoreVal) el.minScoreVal.textContent = '60%';

  if (el.categoryContainer) {
    el.categoryContainer.querySelectorAll('.cat-pill').forEach(btn => {
      btn.classList.toggle('active', btn.getAttribute('data-category') === 'ALL');
    });
  }

  if (el.searchInput) el.searchInput.value = '';
  loadRecommendations();
}

function renderRecommendationsGrid(items) {
  el.recommendationsGrid.innerHTML = '';
  items.forEach(item => {
    const card = createRecommendationCard(item);
    el.recommendationsGrid.appendChild(card);
  });
}

function createRecommendationCard(item) {
  const card = document.createElement('div');
  card.className = 'item-card flex flex-col justify-between rounded-2xl bg-slate-900/60 hover:bg-slate-900/95 border border-slate-800/80 hover:border-slate-700 overflow-hidden shadow-xl transition-all duration-200';

  const category = item.category || 'PRODUCT';
  const categoryConfig = {
    PRODUCT: { icon: 'fa-bag-shopping', badge: 'Product & Pricing', color: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20', btnText: 'Compare 6 Stores', btnIcon: 'fa-code-compare', btnColor: 'bg-emerald-600 hover:bg-emerald-500' },
    MOVIE: { icon: 'fa-film', badge: 'Movie & Media', color: 'bg-purple-500/10 text-purple-400 border-purple-500/20', btnText: 'Watch & Download Links', btnIcon: 'fa-download', btnColor: 'bg-purple-600 hover:bg-purple-500' },
    PLACE: { icon: 'fa-map-location-dot', badge: 'Place & Route', color: 'bg-amber-500/10 text-amber-400 border-amber-500/20', btnText: 'Shortest Route & Map', btnIcon: 'fa-route', btnColor: 'bg-amber-600 hover:bg-amber-500' },
    FOOD: { icon: 'fa-utensils', badge: 'Food & Reviews', color: 'bg-rose-500/10 text-rose-400 border-rose-500/20', btnText: 'Reviews & Sentiment', btnIcon: 'fa-chart-pie', btnColor: 'bg-rose-600 hover:bg-rose-500' },
    WEB: { icon: 'fa-globe', badge: 'Live Synthesis', color: 'bg-cyan-500/10 text-cyan-400 border-cyan-500/20', btnText: 'View Details', btnIcon: 'fa-up-right-from-square', btnColor: 'bg-cyan-600 hover:bg-cyan-500' }
  }[category] || { icon: 'fa-sparkles', badge: category, color: 'bg-slate-700 text-slate-300 border-slate-600', btnText: 'View Details', btnIcon: 'fa-eye', btnColor: 'bg-indigo-600 hover:bg-indigo-500' };

  const matchPercent = Math.round((item.matchScore || 0.85) * 100);
  const starsHtml = renderStarRating(item.rating || 4.8);
  const priceDisplay = item.price ? `$${item.price.toFixed(2)}` : '';
  const aiExpl = item.aiExplanation || 'Tailored to your profile preferences by the multi-domain AI recommendation engine.';
  const factors = item.scoreFactors || { contentSimilarity: 0.45, collaborativeAffinity: 0.35, popularityWeight: 0.20 };

  card.innerHTML = `
    <div>
      <!-- Card Media Header & Overlays -->
      <div class="relative h-44 w-full overflow-hidden bg-slate-950">
        <img src="${escapeHtml(item.imageUrl || 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=600&q=80')}" alt="${escapeHtml(item.title)}" class="w-full h-full object-cover transition duration-300 hover:scale-105" onerror="this.src='https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=600&q=80'">
        <div class="absolute inset-0 bg-gradient-to-t from-slate-950 via-transparent to-black/50"></div>
        
        <!-- Category Tag -->
        <div class="absolute top-3 left-3">
          <span class="px-2.5 py-1 rounded-full text-[10px] font-bold uppercase tracking-wider backdrop-blur-md border ${categoryConfig.color} flex items-center space-x-1.5 shadow">
            <i class="fa-solid ${categoryConfig.icon}"></i>
            <span>${categoryConfig.badge}</span>
          </span>
        </div>

        <!-- AI Match Confidence Badge (Prominent Visual Hierarchy) -->
        <div class="absolute top-3 right-3">
          <span class="px-2.5 py-1 rounded-full text-xs font-extrabold bg-gradient-to-r from-emerald-500 to-teal-400 text-slate-950 backdrop-blur-md shadow-lg shadow-emerald-500/25 flex items-center space-x-1">
            <i class="fa-solid fa-sparkles text-[10px]"></i>
            <span>${matchPercent}% Match</span>
          </span>
        </div>

        <!-- Price Badge if exists -->
        ${priceDisplay ? `<div class="absolute bottom-2.5 right-3 px-2.5 py-0.5 rounded-lg bg-black/85 backdrop-blur-md text-white font-extrabold text-xs border border-slate-800">${priceDisplay}</div>` : ''}
      </div>

      <!-- Card Body Content -->
      <div class="p-4 space-y-3">
        
        <!-- Item Title (Most Prominent Text) -->
        <h3 class="font-extrabold text-base text-white hover:text-indigo-300 transition cursor-pointer line-clamp-1" title="${escapeHtml(item.title)}">
          ${escapeHtml(item.title)}
        </h3>

        <!-- Rating & Review Count -->
        <div class="flex items-center space-x-2 text-xs">
          <div class="flex text-amber-400 text-[11px]">${starsHtml}</div>
          <span class="font-bold text-slate-200">${(item.rating || 4.8).toFixed(1)}</span>
          <span class="text-slate-500 text-[11px]">(${(item.reviewCount || 1000).toLocaleString()} reviews)</span>
        </div>

        <!-- 1-Sentence AI Explanation Box -->
        <div class="p-2.5 rounded-xl bg-slate-950/70 border border-slate-800 text-[11px] text-slate-300 leading-relaxed flex items-start space-x-2">
          <i class="fa-solid fa-wand-magic-sparkles text-indigo-400 mt-0.5 shrink-0 text-xs"></i>
          <span>${escapeHtml(aiExpl)}</span>
        </div>

        <!-- Interactive Feedback Bar & Progressive Disclosure Toggle -->
        <div class="flex items-center justify-between pt-1 border-t border-slate-800/80 text-xs">
          <div class="flex items-center space-x-1.5">
            <button class="feedback-btn like-btn px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-slate-700/80 text-slate-400 hover:text-emerald-400 border border-slate-700/60 transition flex items-center space-x-1" title="Helpful recommendation">
              <i class="fa-solid fa-thumbs-up text-[11px]"></i>
              <span class="text-[10px] font-semibold">Like</span>
            </button>
            <button class="feedback-btn dislike-btn px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-slate-700/80 text-slate-400 hover:text-rose-400 border border-slate-700/60 transition flex items-center space-x-1" title="Less like this">
              <i class="fa-solid fa-thumbs-down text-[11px]"></i>
              <span class="text-[10px] font-semibold">Pass</span>
            </button>
          </div>

          <button class="why-this-toggle text-[11px] text-indigo-400 hover:text-indigo-300 flex items-center space-x-1 transition font-medium">
            <span>Why this?</span>
            <i class="fa-solid fa-chevron-down text-[9px] transition-transform duration-200"></i>
          </button>
        </div>

        <!-- Progressive Disclosure Accordion: Detailed AI Reasoning -->
        <div class="why-this-drawer space-y-1.5 text-[11px] text-slate-400">
          <div class="p-2.5 rounded-xl bg-slate-950/90 border border-slate-800 space-y-1">
            <div class="flex justify-between items-center">
              <span>Content Similarity:</span>
              <span class="font-mono text-indigo-400 font-bold">${Math.round((factors.contentSimilarity || 0.45) * 100)}%</span>
            </div>
            <div class="flex justify-between items-center">
              <span>Collaborative Affinity:</span>
              <span class="font-mono text-purple-400 font-bold">${Math.round((factors.collaborativeAffinity || 0.35) * 100)}%</span>
            </div>
            <div class="flex justify-between items-center">
              <span>Community Popularity:</span>
              <span class="font-mono text-amber-400 font-bold">${Math.round((factors.popularityWeight || 0.20) * 100)}%</span>
            </div>
          </div>
        </div>

      </div>
    </div>

    <!-- Action Button Footer -->
    <div class="p-4 pt-0">
      <button class="action-btn w-full py-2.5 px-3 rounded-xl ${categoryConfig.btnColor} text-white text-xs font-semibold shadow-md flex items-center justify-center space-x-2 transition duration-150">
        <i class="fa-solid ${categoryConfig.btnIcon}"></i>
        <span>${categoryConfig.btnText}</span>
      </button>
    </div>
  `;

  // Attach feedback listeners
  const likeBtn = card.querySelector('.like-btn');
  const dislikeBtn = card.querySelector('.dislike-btn');
  const whyToggle = card.querySelector('.why-this-toggle');
  const whyDrawer = card.querySelector('.why-this-drawer');
  const whyChevron = whyToggle.querySelector('i');

  likeBtn.addEventListener('click', (e) => {
    e.stopPropagation();
    likeBtn.classList.toggle('liked');
    dislikeBtn.classList.remove('disliked');
    recordInteraction(item.id, category, 'LIKE');
    showToast(`Recorded positive feedback for ${item.title}`, 'success');
  });

  dislikeBtn.addEventListener('click', (e) => {
    e.stopPropagation();
    dislikeBtn.classList.toggle('disliked');
    likeBtn.classList.remove('liked');
    recordInteraction(item.id, category, 'DISLIKE');
    showToast(`Understood, showing less recommendations like this`, 'info');
  });

  whyToggle.addEventListener('click', (e) => {
    e.stopPropagation();
    const isOpen = whyDrawer.classList.toggle('open');
    whyChevron.style.transform = isOpen ? 'rotate(180deg)' : 'rotate(0deg)';
  });

  // Attach click listener to action button
  const actionBtn = card.querySelector('.action-btn');
  actionBtn.addEventListener('click', () => {
    recordInteraction(item.id, category, 'CLICK');
    handleItemAction(item);
  });

  return card;
}

function handleItemAction(item) {
  const cat = item.category || 'PRODUCT';
  if (cat === 'PRODUCT') {
    openProductModal(item.id);
  } else if (cat === 'MOVIE') {
    openMovieModal(item.id);
  } else if (cat === 'PLACE') {
    const userLocation = getUserLocationAnchor();
    openRouteModal(userLocation, item.title);
  } else if (cat === 'FOOD') {
    openFoodModal(item.id);
  } else {
    // WEB or fallback
    openProductModal(item.id);
  }
}

function getUserLocationAnchor() {
  const u = state.users.find(u => u.id === state.activeUserId);
  return (u && u.preferences && u.preferences.currentLocation) ? u.preferences.currentLocation : 'Notre Dame, Paris';
}

function renderStarRating(rating) {
  const full = Math.floor(rating);
  const half = rating % 1 >= 0.5 ? 1 : 0;
  const empty = 5 - full - half;
  let html = '';
  for (let i = 0; i < full; i++) html += '<i class="fa-solid fa-star"></i>';
  if (half) html += '<i class="fa-solid fa-star-half-stroke"></i>';
  for (let i = 0; i < empty; i++) html += '<i class="fa-regular fa-star text-slate-600"></i>';
  return html;
}

async function recordInteraction(itemId, category, type) {
  try {
    await fetch(`${API_BASE}/interaction`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        userId: state.activeUserId,
        itemId: itemId,
        category: category,
        type: type
      })
    });
  } catch (err) {
    console.error('Failed to record interaction:', err);
  }
}

// --- MODAL 1: PRODUCT COMPARISON ---
async function openProductModal(itemId) {
  try {
    const res = await fetch(`${API_BASE}/compare?itemId=${encodeURIComponent(itemId)}`);
    const data = await res.json();

    document.getElementById('mProdTitle').textContent = data.productName || 'Product Comparison';
    document.getElementById('mProdBrand').textContent = `Brand: ${data.brand || 'Featured'} • ${data.priceTrend || ''} • Across ${data.storeListings?.length || 6} Stores`;
    document.getElementById('mProdLowestPrice').textContent = `$${(data.lowestPrice || 0).toFixed(2)}`;
    document.getElementById('mProdBestStore').textContent = `Best Deal at ${data.bestStore || 'Store'}`;
    document.getElementById('mProdAvgPrice').textContent = `$${(data.averagePrice || 0).toFixed(2)}`;
    document.getElementById('mProdHighPrice').textContent = `$${(data.highestPrice || 0).toFixed(2)}`;
    document.getElementById('mProdSavings').textContent = `$${(data.maxSavings || 0).toFixed(2)} (${(data.savingsPercentage || 0).toFixed(1)}%)`;

    const tbody = document.getElementById('mProdListingsTable');
    tbody.innerHTML = '';

    (data.storeListings || []).forEach(s => {
      const isLowest = s.price === data.lowestPrice;
      const tr = document.createElement('tr');
      tr.className = `border-b border-slate-800 ${isLowest ? 'bg-emerald-950/20' : 'hover:bg-slate-800/40'} transition`;
      tr.innerHTML = `
        <td class="px-4 py-3">
          <div class="font-bold text-white flex items-center space-x-2">
            <span>${escapeHtml(s.storeName)}</span>
            ${s.badge ? `<span class="px-2 py-0.5 rounded text-[10px] font-semibold ${isLowest ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30' : 'bg-slate-700 text-slate-300'}">${escapeHtml(s.badge)}</span>` : ''}
          </div>
        </td>
        <td class="px-4 py-3 font-extrabold ${isLowest ? 'text-emerald-400 text-sm' : 'text-slate-200'}">
          $${s.price.toFixed(2)}
        </td>
        <td class="px-4 py-3 text-amber-400 font-semibold">
          ★ ${s.rating.toFixed(1)} <span class="text-slate-500 text-[10px]">(${(s.reviewCount || 1000).toLocaleString()})</span>
        </td>
        <td class="px-4 py-3 text-slate-300 text-[11px]">
          <div>${escapeHtml(s.shippingInfo || 'Free Shipping')}</div>
          <span class="text-[10px] ${s.inStock ? 'text-emerald-400 font-medium' : 'text-rose-400'}">${s.inStock ? '● In Stock' : 'Out of stock'}</span>
        </td>
        <td class="px-4 py-3 text-right">
          <a href="${escapeHtml(s.directUrl)}" target="_blank" class="inline-flex items-center space-x-1.5 px-3 py-1.5 rounded-lg ${isLowest ? 'bg-emerald-600 hover:bg-emerald-500 text-white font-bold' : 'bg-slate-700 hover:bg-slate-600 text-slate-200'} text-xs transition">
            <span>Visit Store</span>
            <i class="fa-solid fa-arrow-up-right-from-square text-[10px]"></i>
          </a>
        </td>
      `;
      tbody.appendChild(tr);
    });

    closeAllModals();
    el.modalProduct.classList.remove('hidden');
  } catch (err) {
    console.error('Failed to fetch product comparison:', err);
  }
}

// --- MODAL 2: MOVIE DOWNLOAD & STREAMING ---
async function openMovieModal(itemId) {
  try {
    const res = await fetch(`${API_BASE}/movie?itemId=${encodeURIComponent(itemId)}`);
    const data = await res.json();

    document.getElementById('mMovTitle').textContent = data.title || 'Movie';
    document.getElementById('mMovSubtitle').textContent = `${data.year || 2023} • ${data.genre || 'Action'} • ${data.duration || '2h'} • Directed by ${data.director || 'Director'}`;
    document.getElementById('mMovImdb').textContent = `IMDb: ${(data.imdbRating || 8.5).toFixed(1)}`;
    document.getElementById('mMovRt').textContent = `Rotten Tomatoes: ${data.rottenTomatoesScore || 90}%`;
    document.getElementById('mMovCast').textContent = `Cast: ${(data.cast || []).join(', ')}`;
    document.getElementById('mMovSynopsis').textContent = data.synopsis || 'Synopsis not available.';

    // Download Links
    const downContainer = document.getElementById('mMovDownloadLinks');
    downContainer.innerHTML = '';
    (data.downloadLinks || []).forEach(d => {
      const item = document.createElement('div');
      item.className = 'flex items-center justify-between p-3 rounded-xl bg-slate-800/80 border border-slate-700 hover:border-purple-500/50 transition';
      item.innerHTML = `
        <div class="space-y-0.5">
          <div class="flex items-center space-x-2">
            <span class="font-bold text-white text-xs">${escapeHtml(d.sourceName)}</span>
            <span class="px-2 py-0.5 bg-purple-500/20 text-purple-400 rounded text-[10px] font-semibold">${escapeHtml(d.quality)}</span>
            <span class="text-slate-400 text-[10px] font-mono">${escapeHtml(d.size)}</span>
          </div>
          <div class="text-[11px] text-slate-400">${escapeHtml(d.format)} • Verified Clean Vault</div>
        </div>
        <a href="${escapeHtml(d.directUrl)}" target="_blank" class="px-3.5 py-1.5 rounded-lg bg-purple-600 hover:bg-purple-500 text-white text-xs font-semibold flex items-center space-x-1.5 transition shadow">
          <i class="fa-solid fa-download text-xs"></i>
          <span>Direct Download</span>
        </a>
      `;
      downContainer.appendChild(item);
    });

    // Streaming Platforms
    const streamContainer = document.getElementById('mMovStreamingLinks');
    streamContainer.innerHTML = '';
    (data.streamingPlatforms || []).forEach(s => {
      const item = document.createElement('div');
      item.className = 'flex items-center justify-between p-3 rounded-xl bg-slate-800/80 border border-slate-700 hover:border-indigo-500/50 transition';
      item.innerHTML = `
        <div>
          <div class="font-bold text-white text-xs">${escapeHtml(s.platformName)}</div>
          <div class="text-[10px] text-indigo-400">${escapeHtml(s.streamType)} (${escapeHtml(s.price)})</div>
        </div>
        <a href="${escapeHtml(s.directWatchUrl)}" target="_blank" class="px-3 py-1 rounded-lg bg-slate-700 hover:bg-indigo-600 text-white text-xs font-medium flex items-center space-x-1 transition">
          <span>Watch</span>
          <i class="fa-solid fa-play text-[10px]"></i>
        </a>
      `;
      streamContainer.appendChild(item);
    });

    closeAllModals();
    el.modalMovie.classList.remove('hidden');
  } catch (err) {
    console.error('Failed to fetch movie details:', err);
  }
}

// --- MODAL 3: SHORTEST PATH & MAP ROUTING ---
async function openRouteModal(origin, destination) {
  try {
    el.routeInputOrigin.value = origin;
    el.routeInputDest.value = destination;

    const res = await fetch(`${API_BASE}/route?origin=${encodeURIComponent(origin)}&destination=${encodeURIComponent(destination)}`);
    const data = await res.json();

    document.getElementById('mRouteTitle').textContent = `Shortest Route to ${data.destinationName || destination}`;
    document.getElementById('mRouteDistance').textContent = `${(data.distanceKm || 0).toFixed(1)} km`;
    document.getElementById('mRouteDuration').textContent = data.formattedDuration || 'Calculating...';
    document.getElementById('mRouteGoogleLink').href = data.navigationUrl || '#';

    // Render Steps
    const stepsList = document.getElementById('mRouteStepsList');
    stepsList.innerHTML = '';
    (data.steps || []).forEach((s, idx) => {
      const div = document.createElement('div');
      div.className = 'flex items-center space-x-2 text-xs text-slate-300 p-2 rounded-lg bg-slate-800/60 border border-slate-800';
      div.innerHTML = `
        <span class="w-5 h-5 rounded-full bg-amber-500/20 text-amber-400 flex items-center justify-center font-bold text-[10px]">${idx + 1}</span>
        <span class="flex-grow">${escapeHtml(s.instruction)}</span>
      `;
      stepsList.appendChild(div);
    });

    closeAllModals();
    el.modalRoute.classList.remove('hidden');

    // Initialize or update Leaflet Map
    setTimeout(() => {
      renderRouteMap(data.originLat, data.originLon, data.destinationLat, data.destinationLon, data.pathCoordinates, data.originName, data.destinationName);
    }, 200);

  } catch (err) {
    console.error('Failed to calculate shortest path:', err);
  }
}

function renderRouteMap(lat1, lon1, lat2, lon2, pathCoords, originName, destName) {
  if (!state.map) {
    state.map = L.map('routeMap').setView([lat1, lon1], 13);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors'
    }).addTo(state.map);
  } else {
    state.map.invalidateSize();
  }

  // Clear previous markers & polylines
  state.mapMarkers.forEach(m => state.map.removeLayer(m));
  state.mapMarkers = [];
  if (state.mapRouteLayer) {
    state.map.removeLayer(state.mapRouteLayer);
  }

  // Add markers
  const startMarker = L.marker([lat1, lon1]).addTo(state.map).bindPopup(`<b>Start:</b> ${originName}`);
  const endMarker = L.marker([lat2, lon2]).addTo(state.map).bindPopup(`<b>Destination:</b> ${destName}`);
  state.mapMarkers.push(startMarker, endMarker);

  // Add polyline
  if (pathCoords && pathCoords.length > 0) {
    state.mapRouteLayer = L.polyline(pathCoords, { color: '#f59e0b', weight: 5, opacity: 0.85, lineJoin: 'round' }).addTo(state.map);
    state.map.fitBounds(state.mapRouteLayer.getBounds(), { padding: [30, 30] });
  } else {
    const directBounds = L.latLngBounds([[lat1, lon1], [lat2, lon2]]);
    state.map.fitBounds(directBounds, { padding: [30, 30] });
  }
}

// --- MODAL 4: FOOD REVIEWS & SENTIMENT ---
async function openFoodModal(itemId) {
  try {
    const res = await fetch(`${API_BASE}/food?itemId=${encodeURIComponent(itemId)}`);
    const data = await res.json();

    document.getElementById('mFoodTitle').textContent = data.name || 'Culinary Delight';
    document.getElementById('mFoodCuisine').textContent = `${data.cuisine || 'Gourmet'} • Price Tier: ${data.priceRange || '$$'} • Dietary: ${(data.dietaryTags || []).join(', ')}`;
    document.getElementById('mFoodOverallRating').textContent = `★ ${(data.overallRating || 4.8).toFixed(1)} (${(data.totalReviews || 3500).toLocaleString()} reviews)`;

    // Sentiment Bars
    const pos = data.positivePercent || 85;
    const neu = data.neutralPercent || 10;
    const neg = data.negativePercent || 5;

    document.getElementById('mFoodPosBar').style.width = `${pos}%`;
    document.getElementById('mFoodNeuBar').style.width = `${neu}%`;
    document.getElementById('mFoodNegBar').style.width = `${neg}%`;

    document.getElementById('mFoodPosTxt').textContent = `${pos}% Positive`;
    document.getElementById('mFoodNeuTxt').textContent = `${neu}% Neutral`;
    document.getElementById('mFoodNegTxt').textContent = `${neg}% Critical`;

    // Dishes list
    const dishesList = document.getElementById('mFoodDishesList');
    dishesList.innerHTML = '';
    (data.bestRecommendedDishes || []).forEach(d => {
      const li = document.createElement('li');
      li.className = 'flex items-center space-x-2';
      li.innerHTML = `<i class="fa-solid fa-utensils text-amber-400 text-[10px]"></i><span>${escapeHtml(d)}</span>`;
      dishesList.appendChild(li);
    });

    // Pros list
    const prosList = document.getElementById('mFoodProsList');
    prosList.innerHTML = '';
    (data.pros || []).forEach(p => {
      const li = document.createElement('li');
      li.className = 'flex items-center space-x-2';
      li.innerHTML = `<i class="fa-solid fa-check text-emerald-400 text-[10px]"></i><span>${escapeHtml(p)}</span>`;
      prosList.appendChild(li);
    });

    // Reviews list
    const reviewsList = document.getElementById('mFoodReviewsList');
    reviewsList.innerHTML = '';
    (data.sampleReviews || []).forEach(r => {
      const div = document.createElement('div');
      div.className = 'p-2.5 rounded-xl bg-slate-800/60 border border-slate-700/60 text-xs space-y-1';
      div.innerHTML = `
        <div class="flex items-center justify-between">
          <div class="flex items-center space-x-2">
            <span class="font-bold text-white">${escapeHtml(r.reviewer)}</span>
            <span class="px-1.5 py-0.2 rounded text-[10px] bg-slate-700 text-slate-300">${escapeHtml(r.source)}</span>
          </div>
          <span class="text-amber-400 font-semibold">★ ${r.rating.toFixed(1)}</span>
        </div>
        <p class="text-slate-300 text-[11px] leading-relaxed">"${escapeHtml(r.comment)}"</p>
      `;
      reviewsList.appendChild(div);
    });

    closeAllModals();
    el.modalFood.classList.remove('hidden');
  } catch (err) {
    console.error('Failed to fetch food reviews:', err);
  }
}

// --- MODAL 5: PREFERENCES MANAGEMENT ---
function openPreferencesModal() {
  const user = state.users.find(u => u.id === state.activeUserId);
  if (user && user.preferences) {
    const prefs = user.preferences;
    if (prefs.pricePreference) el.prefPriceSelect.value = prefs.pricePreference;
    if (prefs.dietaryPreference) el.prefDietarySelect.value = prefs.dietaryPreference;
    if (prefs.currentLocation) el.prefLocationInput.value = prefs.currentLocation;

    const catCheckboxes = el.userPreferencesForm.querySelectorAll('input[name="prefCat"]');
    const prefCats = prefs.preferredCategories || [];
    catCheckboxes.forEach(cb => {
      cb.checked = prefCats.includes(cb.value);
    });
  }

  closeAllModals();
  el.modalPreferences.classList.remove('hidden');
}

async function savePreferences(e) {
  e.preventDefault();
  const selectedCats = [];
  el.userPreferencesForm.querySelectorAll('input[name="prefCat"]:checked').forEach(cb => {
    selectedCats.push(cb.value);
  });

  const body = {
    preferredCategories: selectedCats,
    pricePreference: el.prefPriceSelect.value,
    dietaryPreference: el.prefDietarySelect.value,
    currentLocation: el.prefLocationInput.value.trim()
  };

  try {
    const res = await fetch(`${API_BASE}/user/${state.activeUserId}/preferences`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    });
    const data = await res.json();
    if (data.success) {
      closeAllModals();
      showToast('Preferences updated successfully! Feed regenerated.', 'success');
      loadUsers();
      loadRecommendations();
    }
  } catch (err) {
    console.error('Failed to save preferences:', err);
  }
}

function closeAllModals() {
  el.modalProduct.classList.add('hidden');
  el.modalMovie.classList.add('hidden');
  el.modalRoute.classList.add('hidden');
  el.modalFood.classList.add('hidden');
  el.modalPreferences.classList.add('hidden');
}

// --- ADMIN DASHBOARD: SETTINGS & EFFECTIVENESS ANALYSIS ---
async function loadAdminSettings() {
  try {
    const res = await fetch(`${API_BASE}/admin/settings`);
    const data = await res.json();
    bindAdminValuesToInputs(data);
  } catch (err) {
    console.error('Failed to load admin settings:', err);
  }
}

function bindAdminValuesToInputs(data) {
  setSlider('inputContentWeight', 'valContentWeight', data.contentWeight);
  setSlider('inputCollaborativeWeight', 'valCollaborativeWeight', data.collaborativeWeight);
  setSlider('inputPopularityWeight', 'valPopularityWeight', data.popularityWeight);
  setSlider('inputExplorationRate', 'valExplorationRate', data.explorationRate);
  setSlider('inputMinConfidenceThreshold', 'valMinConfidenceThreshold', data.minConfidenceThreshold);

  const liveCheck = document.getElementById('inputEnableLiveWebSearch');
  if (liveCheck) liveCheck.checked = !!data.enableLiveWebSearch;

  if (data.lastUpdatedAt) {
    el.settingsLastUpdated.textContent = new Date(data.lastUpdatedAt).toLocaleTimeString();
  }
}

function setSlider(inputId, valId, value) {
  const input = document.getElementById(inputId);
  const val = document.getElementById(valId);
  if (input && val && value !== undefined) {
    input.value = value;
    val.textContent = parseFloat(value).toFixed(2);
  }
}

async function applyAlgorithmPreset(preset) {
  try {
    const res = await fetch(`${API_BASE}/admin/preset`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ preset: preset })
    });
    const data = await res.json();
    if (data.success && data.settings) {
      bindAdminValuesToInputs(data.settings);
      showToast(`Applied preset: ${preset.toUpperCase()} (Saved to SQLite)`, 'info');
      loadAnalytics();
      loadRecommendations();
      return;
    }
  } catch (err) {
    console.warn('Backend preset API failed, applying locally:', err);
  }

  // Local fallback
  if (preset === 'balanced') {
    setSlider('inputContentWeight', 'valContentWeight', 0.40);
    setSlider('inputCollaborativeWeight', 'valCollaborativeWeight', 0.35);
    setSlider('inputPopularityWeight', 'valPopularityWeight', 0.25);
    setSlider('inputExplorationRate', 'valExplorationRate', 0.15);
    setSlider('inputMinConfidenceThreshold', 'valMinConfidenceThreshold', 0.40);
  } else if (preset === 'discovery') {
    setSlider('inputContentWeight', 'valContentWeight', 0.30);
    setSlider('inputCollaborativeWeight', 'valCollaborativeWeight', 0.25);
    setSlider('inputPopularityWeight', 'valPopularityWeight', 0.45);
    setSlider('inputExplorationRate', 'valExplorationRate', 0.30);
    setSlider('inputMinConfidenceThreshold', 'valMinConfidenceThreshold', 0.30);
  } else if (preset === 'precision' || preset === 'accuracy') {
    setSlider('inputContentWeight', 'valContentWeight', 0.55);
    setSlider('inputCollaborativeWeight', 'valCollaborativeWeight', 0.35);
    setSlider('inputPopularityWeight', 'valPopularityWeight', 0.10);
    setSlider('inputExplorationRate', 'valExplorationRate', 0.05);
    setSlider('inputMinConfidenceThreshold', 'valMinConfidenceThreshold', 0.60);
  } else if (preset === 'diversity') {
    setSlider('inputContentWeight', 'valContentWeight', 0.25);
    setSlider('inputCollaborativeWeight', 'valCollaborativeWeight', 0.50);
    setSlider('inputPopularityWeight', 'valPopularityWeight', 0.25);
    setSlider('inputExplorationRate', 'valExplorationRate', 0.25);
    setSlider('inputMinConfidenceThreshold', 'valMinConfidenceThreshold', 0.35);
  } else if (preset === 'deals') {
    setSlider('inputContentWeight', 'valContentWeight', 0.30);
    setSlider('inputCollaborativeWeight', 'valCollaborativeWeight', 0.30);
    setSlider('inputPopularityWeight', 'valPopularityWeight', 0.40);
    setSlider('inputExplorationRate', 'valExplorationRate', 0.20);
    setSlider('inputMinConfidenceThreshold', 'valMinConfidenceThreshold', 0.35);
  }
  showToast(`Applied preset: ${preset.toUpperCase()}`, 'info');
}

async function saveAdminSettings() {
  const payload = {
    contentWeight: parseFloat(document.getElementById('inputContentWeight').value),
    collaborativeWeight: parseFloat(document.getElementById('inputCollaborativeWeight').value),
    popularityWeight: parseFloat(document.getElementById('inputPopularityWeight').value),
    explorationRate: parseFloat(document.getElementById('inputExplorationRate').value),
    minConfidenceThreshold: parseFloat(document.getElementById('inputMinConfidenceThreshold').value),
    enableLiveWebSearch: document.getElementById('inputEnableLiveWebSearch').checked
  };

  try {
    const res = await fetch(`${API_BASE}/admin/settings`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    const data = await res.json();
    if (data.success) {
      showToast('Settings saved! Algorithms updated dynamically.', 'success');
      bindAdminValuesToInputs(data.settings);
      loadAnalytics();
      loadRecommendations();
    }
  } catch (err) {
    showToast('Failed to save settings: ' + err.message, 'error');
  }
}

async function resetAdminSettings() {
  try {
    const res = await fetch(`${API_BASE}/admin/reset`, { method: 'POST' });
    const data = await res.json();
    if (data.success) {
      showToast('System settings reset to default values.', 'info');
      bindAdminValuesToInputs(data.settings);
      loadAnalytics();
      loadRecommendations();
    }
  } catch (err) {
    showToast('Failed to reset settings: ' + err.message, 'error');
  }
}

async function loadAnalytics() {
  try {
    const res = await fetch(`${API_BASE}/admin/analytics`);
    const data = await res.json();

    // KPI Counters
    document.getElementById('kpiImpressions').textContent = (data.totalImpressions || 0).toLocaleString();
    document.getElementById('kpiClicks').textContent = (data.totalClicks || 0).toLocaleString();
    document.getElementById('kpiCtr').textContent = `${(data.overallCtr || 0).toFixed(1)}%`;
    document.getElementById('kpiConversion').textContent = `${(data.conversionRate || 0).toFixed(1)}%`;
    document.getElementById('kpiPrecision').textContent = (data.precisionAtK || 0.82).toFixed(3);
    document.getElementById('kpiDiversity').textContent = `${Math.round((data.diversityScore || 0.85) * 100)}%`;

    // AI Suggestions
    el.aiSuggestionsList.innerHTML = '';
    (data.aiOptimizationSuggestions || []).forEach(s => {
      const p = document.createElement('p');
      p.className = 'flex items-start space-x-2 p-2 rounded-lg bg-slate-800/80 border border-slate-700/60';
      p.innerHTML = `<i class="fa-solid fa-lightbulb text-amber-400 mt-0.5"></i><span>${escapeHtml(s)}</span>`;
      el.aiSuggestionsList.appendChild(p);
    });

    // Render Charts
    renderCategoryCtrChart(data.categoryMetrics || {});
    renderTimelineChart(data.timelineMetrics || []);

  } catch (err) {
    console.error('Failed to load analytics:', err);
  }
}

function renderCategoryCtrChart(catMetrics) {
  const ctx = document.getElementById('chartCategoryCtr').getContext('2d');
  const labels = Object.keys(catMetrics);
  const data = labels.map(k => catMetrics[k].ctr);

  if (state.charts.categoryCtr) {
    state.charts.categoryCtr.destroy();
  }

  state.charts.categoryCtr = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: labels,
      datasets: [{
        label: 'CTR (%)',
        data: data,
        backgroundColor: [
          'rgba(99, 102, 241, 0.7)',
          'rgba(168, 85, 247, 0.7)',
          'rgba(245, 158, 11, 0.7)',
          'rgba(244, 63, 94, 0.7)'
        ],
        borderRadius: 8
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false }
      },
      scales: {
        y: {
          beginAtZero: true,
          ticks: { color: '#94a3b8', callback: v => v + '%' },
          grid: { color: 'rgba(51, 65, 85, 0.4)' }
        },
        x: {
          ticks: { color: '#94a3b8' },
          grid: { display: false }
        }
      }
    }
  });
}

function renderTimelineChart(timeline) {
  const ctx = document.getElementById('chartTimeline').getContext('2d');
  const labels = timeline.map(t => t.date);
  const impressions = timeline.map(t => t.impressions);
  const clicks = timeline.map(t => t.clicks);

  if (state.charts.timeline) {
    state.charts.timeline.destroy();
  }

  state.charts.timeline = new Chart(ctx, {
    type: 'line',
    data: {
      labels: labels,
      datasets: [
        {
          label: 'Impressions',
          data: impressions,
          borderColor: '#6366f1',
          backgroundColor: 'rgba(99, 102, 241, 0.1)',
          fill: true,
          tension: 0.35
        },
        {
          label: 'Engaged Clicks',
          data: clicks,
          borderColor: '#10b981',
          backgroundColor: 'rgba(16, 185, 129, 0.1)',
          fill: true,
          tension: 0.35
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          labels: { color: '#cbd5e1' }
        }
      },
      scales: {
        y: {
          beginAtZero: true,
          ticks: { color: '#94a3b8' },
          grid: { color: 'rgba(51, 65, 85, 0.4)' }
        },
        x: {
          ticks: { color: '#94a3b8' },
          grid: { display: false }
        }
      }
    }
  });
}

function showToast(message, type = 'success') {
  el.adminToastMessage.textContent = message;
  el.adminToast.classList.remove('hidden');

  if (type === 'success') {
    el.adminToast.className = 'p-4 rounded-xl border border-emerald-500/30 bg-emerald-950/40 text-emerald-300 flex items-center space-x-3 text-sm';
  } else if (type === 'info') {
    el.adminToast.className = 'p-4 rounded-xl border border-indigo-500/30 bg-indigo-950/40 text-indigo-300 flex items-center space-x-3 text-sm';
  } else {
    el.adminToast.className = 'p-4 rounded-xl border border-rose-500/30 bg-rose-950/40 text-rose-300 flex items-center space-x-3 text-sm';
  }

  setTimeout(() => {
    el.adminToast.classList.add('hidden');
  }, 4000);
}

function escapeHtml(str) {
  if (str === null || str === undefined) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}
