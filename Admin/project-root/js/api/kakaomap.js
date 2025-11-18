(function () {
    const SDK_BASE_URL = 'https://dapi.kakao.com/v2/maps/sdk.js';
    const SCRIPT_ID = 'kakao-map-sdk';
    let kakaoScriptPromise = null;

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

    const initMap = () => {
        const mapContainer = document.getElementById('map');
        if (!mapContainer) {
            console.error('#map 요소를 찾을 수 없습니다.');
            return;
        }

        const centerPosition = new kakao.maps.LatLng(35.1649206, 128.0993567); // 서울 시청
        const mapOptions = {
            center: centerPosition,
            level: 3
        };

        const map = new kakao.maps.Map(mapContainer, mapOptions);
        new kakao.maps.Marker({ position: centerPosition, map });

        return map;
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
