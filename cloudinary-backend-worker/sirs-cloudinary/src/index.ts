import { createRemoteJWKSet, jwtVerify } from 'jose';

export interface Env {
  FIREBASE_PROJECT_ID: string;
  CLOUDINARY_CLOUD_NAME: string;
  CLOUDINARY_API_KEY: string;
  CLOUDINARY_API_SECRET: string;
}

const FIREBASE_JWKS_URL = 'https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com';
const JWKS = createRemoteJWKSet(new URL(FIREBASE_JWKS_URL));

export default {
  async fetch(request: Request, env: Env, ctx: ExecutionContext): Promise<Response> {
    const corsHeaders = {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'POST, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type, Authorization',
    };

    if (request.method === 'OPTIONS') {
      return new Response(null, { headers: corsHeaders });
    }

    if (request.method !== 'POST' && request.method !== 'GET') {
      return new Response('Method Not Allowed', { status: 405, headers: corsHeaders });
    }

    try {
      const authHeader = request.headers.get('Authorization') || '';
      if (!authHeader.startsWith('Bearer ')) {
        return new Response(JSON.stringify({ error: 'Unauthorized: Missing or invalid token format' }), { 
          status: 401, headers: { ...corsHeaders, 'Content-Type': 'application/json' } 
        });
      }
      
      const token = authHeader.replace('Bearer ', '');
      const projectId = env.FIREBASE_PROJECT_ID; 
      
      try {
        await jwtVerify(token, JWKS, {
          issuer: `https://securetoken.google.com/${projectId}`,
          audience: projectId,
        });
      } catch (e: any) {
        return new Response(JSON.stringify({ error: `Unauthorized: Invalid token (${e.message})` }), { 
          status: 401, headers: { ...corsHeaders, 'Content-Type': 'application/json' } 
        });
      }

      const url = new URL(request.url);
      
      // ENDPOINT 1: GENERATE SIGNATURE UNTUK UPLOAD
      if (url.pathname === '/generate-signature' && request.method === 'GET') {
        const timestamp = Math.floor(Date.now() / 1000).toString();
        const folder = 'incident-reports';

        // Urutan parameter sesuai abjad (Cloudinary standard): folder, timestamp
        const signatureString = `folder=${folder}&timestamp=${timestamp}${env.CLOUDINARY_API_SECRET}`;
        
        const encoder = new TextEncoder();
        const data = encoder.encode(signatureString);
        const hashBuffer = await crypto.subtle.digest('SHA-1', data);
        const hashArray = Array.from(new Uint8Array(hashBuffer));
        const signature = hashArray.map(b => b.toString(16).padStart(2, '0')).join('');

        return new Response(JSON.stringify({
          signature: signature,
          timestamp: timestamp,
          api_key: env.CLOUDINARY_API_KEY,
          folder: folder,
          cloud_name: env.CLOUDINARY_CLOUD_NAME
        }), {
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        });
      }

      // ENDPOINT 2: MENGHAPUS GAMBAR (Yang lama)
      if (request.method === 'POST') {
        const body = await request.json() as any;
        const publicId = body.publicId;
      const resourceType = body.resourceType || 'image';

      if (!publicId) {
        return new Response(JSON.stringify({ error: 'Bad Request: publicId required' }), { 
          status: 400, headers: { ...corsHeaders, 'Content-Type': 'application/json' } 
        });
      }

      const timestamp = Math.floor(Date.now() / 1000).toString();
      const signatureString = `public_id=${publicId}&timestamp=${timestamp}${env.CLOUDINARY_API_SECRET}`;
      
      const encoder = new TextEncoder();
      const data = encoder.encode(signatureString);
      const hashBuffer = await crypto.subtle.digest('SHA-1', data);
      const hashArray = Array.from(new Uint8Array(hashBuffer));
      const signature = hashArray.map(b => b.toString(16).padStart(2, '0')).join('');

      const formData = new FormData();
      formData.append('public_id', publicId);
      formData.append('timestamp', timestamp);
      formData.append('api_key', env.CLOUDINARY_API_KEY);
      formData.append('signature', signature);

      const cloudUrl = `https://api.cloudinary.com/v1_1/${env.CLOUDINARY_CLOUD_NAME}/${resourceType}/destroy`;
      
      const cloudRes = await fetch(cloudUrl, {
        method: 'POST',
        body: formData
      });

        const cloudResult = await cloudRes.json();
        return new Response(JSON.stringify(cloudResult), {
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        });
      }

      return new Response('Not Found', { status: 404, headers: corsHeaders });

    } catch (e: any) {
      return new Response(JSON.stringify({ error: e.message }), { 
        status: 500, headers: { 'Access-Control-Allow-Origin': '*', 'Content-Type': 'application/json' } 
      });
    }
  }
};
