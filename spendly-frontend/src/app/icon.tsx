import { ImageResponse } from "next/og";

export const size = {
  width: 512,
  height: 512,
};

export const contentType = "image/png";

export default function Icon() {
  return new ImageResponse(
    (
      <svg width="512" height="512" viewBox="0 0 512 512" fill="none">
        <rect width="512" height="512" rx="112" fill="#F3EFE5" />
        <path
          d="M256 72C218 99 176 119 129 132C114 136 104 149 104 165V274C104 347 151 413 256 456C361 413 408 347 408 274V165C408 149 398 136 383 132C336 119 294 99 256 72Z"
          fill="#13261A"
        />
        <path
          d="M256 124C226 143 193 158 157 168C149 170 144 177 144 185V275C144 331 179 380 256 416C333 380 368 331 368 275V185C368 177 363 170 355 168C319 158 286 143 256 124Z"
          fill="#FFF8EA"
        />
        <path
          d="M256 154C232 170 205 182 176 190C169 192 164 198 164 206V254H368V206C368 198 363 192 356 190C327 182 300 170 276 154C270 150 262 150 256 154Z"
          fill="#86B393"
          fillOpacity="0.55"
        />
        <path
          d="M164 254H368V276C368 321 340 360 256 397C172 360 144 321 144 276V254H164Z"
          fill="#236948"
        />
        <path
          d="M95 223H211C246 223 272 232 283 250C297 273 279 295 241 312C216 323 203 336 203 352C203 369 219 382 249 398"
          stroke="#FFF8EA"
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth="42"
        />
        <path
          d="M95 223H211C246 223 272 232 283 250C297 273 279 295 241 312C216 323 203 336 203 352C203 369 219 382 249 398"
          stroke="#DA6A35"
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth="28"
        />
        <path
          d="M252 316L282 346L340 279"
          stroke="#FFF8EA"
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth="48"
        />
        <path
          d="M252 316L282 346L340 279"
          stroke="#DA6A35"
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth="30"
        />
      </svg>
    ),
    size,
  );
}
