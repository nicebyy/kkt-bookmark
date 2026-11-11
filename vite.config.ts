import { defineConfig, Plugin } from 'vite';
import react from '@vitejs/plugin-react';
import fs from 'fs';
import path from 'path';

// .apk 요청 시 Android 패키지 다운로드 헤더 강제 설정 플러그인
const apkServePlugin = (): Plugin => ({
  name: 'apk-serve-plugin',
  configureServer(server) {
    server.middlewares.use((req, res, next) => {
      const url = req.url || '';
      if (url.includes('kkt-bookmark.apk') || url.includes('download-apk')) {
        const filePath = path.resolve(__dirname, 'public/kkt-bookmark.apk');
        if (fs.existsSync(filePath)) {
          const stat = fs.statSync(filePath);
          res.writeHead(200, {
            'Content-Type': 'application/vnd.android.package-archive',
            'Content-Length': stat.size,
            'Content-Disposition': 'attachment; filename="kkt-bookmark.apk"',
            'Cache-Control': 'no-cache, no-store, must-revalidate',
            'Pragma': 'no-cache',
            'Expires': '0'
          });
          fs.createReadStream(filePath).pipe(res);
          return;
        }
      }
      next();
    });
  }
});

export default defineConfig({
  base: './',
  plugins: [react(), apkServePlugin()],
  server: {
    port: 3002,
    strictPort: true,
    open: false,
    allowedHosts: true,
    watch: {
      ignored: ['**/*.apk', '**/*.zip', '**/*.log', '**/gradle/**', '**/app/**', '**/local.properties']
    }
  }
});
