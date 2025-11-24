(function () {
  const SDK_BASE_URL = "https://dapi.kakao.com/v2/maps/sdk.js";
  const SCRIPT_ID = "kakao-map-sdk";
  let kakaoScriptPromise = null;
  let mapInstance = null;
  let markers = [];
  let pendingMarkers = null;
  let overlays = [];
  let activeOverlay = null;
  let markerLookup = new Map();
  let overlayStylesInjected = false;
  const markerSelectCallbackName = "handleMapMarkerSelect";
  const markerClearCallbackName = "handleMapMarkerClear";
  const USER_VIEW_HOLD_MS = 10000;
  let userViewHoldUntil = 0;
  let suppressNextZoomHold = false;
  const DEFAULT_MARKER_ICON = {
    src: "https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/markerStar.png",
    size: { width: 24, height: 35 },
  };
  const FOCUSED_MARKER_ICON = {
    src: "https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/sign-info-64.png",
    size: { width: 44, height: 48 },
  };
  let defaultMarkerImage = null;
  let focusedMarkerImage = null;
  let activeMarkerId = null;

  const ensureScript = () => {
    if (
      !window.KAKAO_MAP_APP_KEY ||
      window.KAKAO_MAP_APP_KEY === "YOUR_APP_KEY_HERE"
    ) {
      return Promise.reject(
        new Error("config.js에 카카오맵 app key를 설정해주세요.")
      );
    }

    if (document.getElementById(SCRIPT_ID)) {
      return Promise.resolve();
    }

    kakaoScriptPromise =
      kakaoScriptPromise ||
      new Promise((resolve, reject) => {
        const script = document.createElement("script");
        script.id = SCRIPT_ID;
        script.async = true;

        const sdkUrl = new URL(SDK_BASE_URL);
        sdkUrl.searchParams.set("autoload", "false");
        sdkUrl.searchParams.set("appkey", window.KAKAO_MAP_APP_KEY);

        script.src = sdkUrl.toString();
        script.onload = resolve;
        script.onerror = () =>
          reject(new Error("카카오맵 SDK를 불러오지 못했습니다."));
        document.head.appendChild(script);
      });

    return kakaoScriptPromise;
  };

  const defaultCenter = { lat: 35.1649206, lng: 128.0993567 };

  const truncateText = (value = "", limit = 120) => {
    const str = String(value);
    if (str.length <= limit) return str;
    return `${str.slice(0, Math.max(0, limit - 3))}...`;
  };

  const formatDateTime = (value) => {
    if (!value) return "";
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return "";
    return date.toLocaleString("ko-KR", {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const ensureOverlayStyles = () => {
    if (overlayStylesInjected) return;
    const style = document.createElement("style");
    style.id = "complaint-overlay-style";
    style.textContent = `
      .complaint-overlay {
        position: relative;
        width: 220px;
        background: #fff;
        border: 1px solid #c8d2f4;
        border-radius: 12px;
        box-shadow: 0 8px 18px rgba(15, 23, 42, 0.18);
        overflow: hidden;
        font-family: "Noto Sans KR", Arial, sans-serif;
      }

      .complaint-overlay .overlay-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 0.5rem 0.75rem;
        background: linear-gradient(135deg, #5b7cfa, #4d64f3);
        color: #fff;
        font-weight: 600;
        font-size: 0.9rem;
      }

      .complaint-overlay .overlay-close {
        border: none;
        background: transparent;
        color: inherit;
        font-size: 1rem;
        cursor: pointer;
        padding: 0;
        line-height: 1;
      }

      .complaint-overlay .overlay-body {
        padding: 0.75rem;
        color: #1f2a44;
        display: flex;
        flex-direction: column;
        gap: 0.35rem;
      }

      .complaint-overlay .overlay-location {
        font-weight: 600;
        margin: 0;
      }

      .complaint-overlay .overlay-desc {
        font-size: 0.85rem;
        color: #394060;
        margin: 0;
      }

      .complaint-overlay .overlay-desc.empty {
        color: #9aa0b4;
        font-style: italic;
      }

      .complaint-overlay .overlay-author,
      .complaint-overlay .overlay-date {
        font-size: 0.78rem;
        color: #6c7590;
        margin: 0;
      }
    `;
    document.head.appendChild(style);
    overlayStylesInjected = true;
  };

  const createMarkerImage = (icon) => {
    const { width, height } = icon.size;
    return new kakao.maps.MarkerImage(
      icon.src,
      new kakao.maps.Size(width, height),
      {
        offset: new kakao.maps.Point(width / 2, height),
      }
    );
  };

  const ensureMarkerImages = () => {
    if (!window.kakao || !window.kakao.maps) return;
    if (!defaultMarkerImage) {
      defaultMarkerImage = createMarkerImage(DEFAULT_MARKER_ICON);
    }
    if (!focusedMarkerImage) {
      focusedMarkerImage = createMarkerImage(FOCUSED_MARKER_ICON);
    }
  };

  const setMarkerImage = (lookupKey, image) => {
    if (!Number.isFinite(lookupKey)) return false;
    const entry = markerLookup.get(lookupKey);
    if (!entry || !entry.marker || !image) return false;
    entry.marker.setImage(image);
    return true;
  };

  const highlightMarker = (lookupKey) => {
    ensureMarkerImages();
    if (activeMarkerId === lookupKey) return;

    if (Number.isFinite(activeMarkerId)) {
      setMarkerImage(activeMarkerId, defaultMarkerImage);
    }

    if (!Number.isFinite(lookupKey)) {
      activeMarkerId = null;
      return;
    }

    if (setMarkerImage(lookupKey, focusedMarkerImage)) {
      activeMarkerId = lookupKey;
    } else {
      activeMarkerId = null;
    }
  };

  const notifyMarkerClear = (payload) => {
    try {
      if (!window.parent || window.parent === window) return;
      const handler = window.parent[markerClearCallbackName];
      if (typeof handler === "function") {
        handler(payload);
      }
    } catch (error) {
      console.warn("마커 클리어 알림 전달 실패", error);
    }
  };

  const closeOverlay = (overlay, options = {}) => {
    if (!overlay) return;
    const { suppressNotify = false } = options;
    const payload = overlay.__markerPayload;
    overlay.setMap(null);
    if (activeOverlay === overlay) {
      activeOverlay = null;
      highlightMarker(null);
      if (!suppressNotify) {
        notifyMarkerClear(payload);
      }
    }
  };

  const hideActiveOverlay = (options = {}) =>
    closeOverlay(activeOverlay, options);

  const holdUserView = () => {
    userViewHoldUntil = Date.now() + USER_VIEW_HOLD_MS;
  };

  const isUserViewHeld = () => Date.now() < userViewHoldUntil;

  const notifyMarkerSelect = (payload) => {
    try {
      if (!window.parent || window.parent === window) return;
      const handler = window.parent[markerSelectCallbackName];
      if (typeof handler === "function") {
        handler(payload);
      }
    } catch (error) {
      console.warn("마커 선택 알림 전달 실패", error);
    }
  };

  const attachUserInteractionGuards = () => {
    if (!mapInstance) return;
    kakao.maps.event.addListener(mapInstance, "dragstart", holdUserView);
    kakao.maps.event.addListener(mapInstance, "zoom_changed", () => {
      if (suppressNextZoomHold) {
        suppressNextZoomHold = false;
        return;
      }
      holdUserView();
    });
  };

  const createOverlay = (item = {}, position) => {
    ensureOverlayStyles();
    const container = document.createElement("div");
    container.className = "complaint-overlay";

    const header = document.createElement("div");
    header.className = "overlay-header";

    const titleEl = document.createElement("span");
    titleEl.className = "overlay-title";
    titleEl.textContent =
      item.title || item.locationName || item.authorName || "민원 위치";
    header.appendChild(titleEl);

    const closeBtn = document.createElement("button");
    closeBtn.type = "button";
    closeBtn.className = "overlay-close";
    closeBtn.setAttribute("aria-label", "닫기");
    closeBtn.textContent = "X";
    header.appendChild(closeBtn);

    const body = document.createElement("div");
    body.className = "overlay-body";

    if (item.locationName) {
      const locationEl = document.createElement("p");
      locationEl.className = "overlay-location";
      locationEl.textContent = item.locationName;
      body.appendChild(locationEl);
    }

    if (item.authorName) {
      const authorEl = document.createElement("p");
      authorEl.className = "overlay-author";
      authorEl.textContent = `신고자: ${item.authorName}`;
      body.appendChild(authorEl);
    }

    const descEl = document.createElement("p");
    descEl.className = `overlay-desc${item.description ? "" : " empty"}`;
    if (item.description) {
      descEl.textContent = truncateText(item.description, 140);
    } else {
      descEl.textContent = "설명 없음";
    }
    body.appendChild(descEl);

    const createdText = formatDateTime(item.createdAt);
    if (createdText) {
      const dateEl = document.createElement("p");
      dateEl.className = "overlay-date";
      dateEl.textContent = `접수: ${createdText}`;
      body.appendChild(dateEl);
    }

    container.appendChild(header);
    container.appendChild(body);

    const overlay = new kakao.maps.CustomOverlay({
      content: container,
      position,
      yAnchor: 1.4,
      xAnchor: 0.5,
      clickable: true,
    });
    overlay.__markerPayload = item;

    closeBtn.addEventListener("click", (event) => {
      event.preventDefault();
      closeOverlay(overlay);
    });

    return overlay;
  };

  const clearMarkers = () => {
    hideActiveOverlay({ suppressNotify: true });
    if (markers.length) {
      markers.forEach((marker) => marker.setMap(null));
      markers = [];
    }

    if (overlays.length) {
      overlays.forEach((overlay) => {
        if (overlay === activeOverlay) return;
        overlay.setMap(null);
      });
      overlays = [];
    }

    markerLookup.clear();
    activeMarkerId = null;
  };

  const renderMarkers = (locations = []) => {
    if (!mapInstance) {
      pendingMarkers = locations;
      return;
    }

    clearMarkers();
    ensureMarkerImages();

    if (!Array.isArray(locations) || !locations.length) {
      if (!isUserViewHeld()) {
        suppressNextZoomHold = true;
        mapInstance.setLevel(3);
        mapInstance.setCenter(
          new kakao.maps.LatLng(defaultCenter.lat, defaultCenter.lng)
        );
      }
      return;
    }

    const bounds = new kakao.maps.LatLngBounds();

    locations.forEach((item) => {
      const latNum = Number(item?.latitude);
      const lngNum = Number(item?.longitude);
      if (!Number.isFinite(latNum) || !Number.isFinite(lngNum)) return;

      const position = new kakao.maps.LatLng(latNum, lngNum);
      const marker = new kakao.maps.Marker({
        map: mapInstance,
        position,
        title: item?.title || "민원 위치",
        image: defaultMarkerImage,
      });
      markers.push(marker);

      const overlay = createOverlay(item, position);
      overlays.push(overlay);
      const lookupKey = Number(item?.id);
      if (Number.isFinite(lookupKey)) {
        markerLookup.set(lookupKey, {
          overlay,
          position,
          payload: item,
          marker,
        });
      }

      kakao.maps.event.addListener(marker, "click", () => {
        hideActiveOverlay({ suppressNotify: true });
        overlay.setMap(mapInstance);
        activeOverlay = overlay;
        if (typeof mapInstance.panTo === "function") {
          mapInstance.panTo(position);
        }
        highlightMarker(lookupKey);
        notifyMarkerSelect(item);
      });
      bounds.extend(position);
    });

    if (!bounds.isEmpty() && !isUserViewHeld()) {
      suppressNextZoomHold = true;
      mapInstance.setBounds(bounds, 40);
    }
  };

  const initMap = () => {
    const mapContainer = document.getElementById("map");
    if (!mapContainer) {
      console.error("#map 요소를 찾을 수 없습니다.");
      return;
    }

    const centerPosition = new kakao.maps.LatLng(
      defaultCenter.lat,
      defaultCenter.lng
    );
    const mapOptions = {
      center: centerPosition,
      level: 3,
    };

    mapInstance = new kakao.maps.Map(mapContainer, mapOptions);

    kakao.maps.event.addListener(mapInstance, "click", () =>
      hideActiveOverlay()
    );
    attachUserInteractionGuards();

    if (Array.isArray(pendingMarkers)) {
      renderMarkers(pendingMarkers);
      pendingMarkers = null;
    }

    return mapInstance;
  };

  const loadKakaoMap = () => {
    if (window.kakao && window.kakao.maps) {
      window.kakao.maps.load(initMap);
      return;
    }

    ensureScript()
      .then(() => window.kakao.maps.load(initMap))
      .catch((error) => console.error(error.message));
  };

  window.loadKakaoMap = loadKakaoMap;
  window.renderComplaintMarkers = renderMarkers;
  window.focusComplaintMarker = (complaintId) => {
    if (!mapInstance) return;
    const lookupKey = Number(complaintId);
    if (!Number.isFinite(lookupKey)) return;
    const target = markerLookup.get(lookupKey);
    if (!target) return;

    if (activeOverlay?.__markerPayload?.id === lookupKey) {
      hideActiveOverlay();
      return;
    }
    const { overlay, position } = target;

    hideActiveOverlay({ suppressNotify: true });
    overlay.setMap(mapInstance);
    activeOverlay = overlay;
    if (position && typeof mapInstance.panTo === "function") {
      mapInstance.panTo(position);
    }
    highlightMarker(lookupKey);
    holdUserView();
  };

  window.clearComplaintMarkerFocus = () => {
    if (!mapInstance) return;
    hideActiveOverlay();
    highlightMarker(null);
  };
})();
