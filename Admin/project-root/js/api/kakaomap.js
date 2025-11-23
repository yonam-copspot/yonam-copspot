(function () {
    const SDK_BASE_URL = 'https://dapi.kakao.com/v2/maps/sdk.js';
    const SCRIPT_ID = 'kakao-map-sdk';
    const DEFAULT_COORD = { lat: 35.1649206, lng: 128.0993567 };

    let kakaoScriptPromise = null;
    let mapInstance = null;
    let markerInstance = null;

    const ensureScript = () => {
        if (!window.KAKAO_MAP_APP_KEY || window.KAKAO_MAP_APP_KEY === 'YOUR_APP_KEY_HERE') {
            return Promise.reject(new Error('config.js에 카카오맵 app key를 설정해주세요.'));
        }

        if (document.getElementById(SCRIPT_ID)) {
            return Promise.resolve();
        }

        kakaoScriptPromise = kakaoScriptPromise || new Promise((resolve, reject) => {
            const script = document.createElement('script');
            script.id = SCRIPT_ID;
            script.async = true;

            const sdkUrl = new URL(SDK_BASE_URL);
            sdkUrl.searchParams.set('autoload', 'false');
            sdkUrl.searchParams.set('appkey', window.KAKAO_MAP_APP_KEY);

            script.src = sdkUrl.toString();
            script.onload = resolve;
            script.onerror = () => reject(new Error('카카오맵 SDK를 불러오지 못했습니다.'));
            document.head.appendChild(script);
        });

        return kakaoScriptPromise;
    };

    const setStatus = (message, isError = false) => {
        const statusEl = document.getElementById('status-msg');
        if (!statusEl) return;
        statusEl.textContent = message || '';
        statusEl.classList.toggle('error', isError);
    };

    const placeMarker = (lat, lng) => {
        if (!mapInstance || !window.kakao || !window.kakao.maps) {
            setStatus('지도 준비 중입니다. 잠시 후 다시 시도하세요.', true);
            return;
        }

        const position = new kakao.maps.LatLng(lat, lng);
        if (!markerInstance) {
            markerInstance = new kakao.maps.Marker({ map: mapInstance });
        }
        markerInstance.setPosition(position);
        mapInstance.setCenter(position);
        setStatus(`마커 이동: ${lat.toFixed(6)}, ${lng.toFixed(6)}`);
    };

    const bindControls = () => {
        const form = document.getElementById('coords-form');
        const latInput = document.getElementById('lat-input');
        const lngInput = document.getElementById('lng-input');
        const resetBtn = document.getElementById('reset-btn');

        if (!form || !latInput || !lngInput) return;

        latInput.value = DEFAULT_COORD.lat;
        lngInput.value = DEFAULT_COORD.lng;

        form.addEventListener('submit', (event) => {
            event.preventDefault();
            const lat = parseFloat(latInput.value);
            const lng = parseFloat(lngInput.value);

            if (!Number.isFinite(lat) || !Number.isFinite(lng)) {
                setStatus('위도와 경도를 숫자로 입력하세요.', true);
                return;
            }
            placeMarker(lat, lng);
        });

        if (resetBtn) {
            resetBtn.addEventListener('click', () => {
                latInput.value = DEFAULT_COORD.lat;
                lngInput.value = DEFAULT_COORD.lng;
                placeMarker(DEFAULT_COORD.lat, DEFAULT_COORD.lng);
            });
        }
    };

    const initMap = () => {
        const mapContainer = document.getElementById('map');
        if (!mapContainer) {
            console.error('#map 요소를 찾을 수 없습니다.');
            return;
        }

        const centerPosition = new kakao.maps.LatLng(DEFAULT_COORD.lat, DEFAULT_COORD.lng);
        const mapOptions = {
            center: centerPosition,
            level: 3
        };

        mapInstance = new kakao.maps.Map(mapContainer, mapOptions);
        placeMarker(DEFAULT_COORD.lat, DEFAULT_COORD.lng);
        bindControls();

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
})();
