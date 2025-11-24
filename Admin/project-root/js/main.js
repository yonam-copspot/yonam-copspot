document.addEventListener('DOMContentLoaded', () => {
    if (typeof window.loadKakaoMap === 'function') {
        window.loadKakaoMap();
    } else {
        console.error('카카오맵 로더가 초기화되지 않았습니다.');
    }
});
